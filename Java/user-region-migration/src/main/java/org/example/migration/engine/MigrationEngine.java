package org.example.migration.engine;

import org.example.migration.client.RegionClientRegistry;
import org.example.migration.config.MigrationProperties;
import org.example.migration.domain.Direction;
import org.example.migration.domain.RegionName;
import org.example.migration.domain.RunStatus;
import org.example.migration.domain.TenantStatus;
import org.example.migration.domain.entity.MigrationRun;
import org.example.migration.spi.CutoverAction;
import org.example.migration.spi.MigrationContext;
import org.example.migration.spi.TenantMigrationTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 迁移引擎——框架内核。编排分批/并发/断点/对账/切流。
 *
 * 通过 Builder 模式装配依赖，内部委托给 TenantBatcher（分批并发）、
 * RetryStrategy（重试）、TokenBucketRateLimiter（限流）。
 *
 * 对账与切流通过注入的 ReconciliationGate / CutoverAction / MigrationNotifier 决策。
 */
public class MigrationEngine {

    private static final Logger log = LoggerFactory.getLogger(MigrationEngine.class);

    // 必需依赖
    private final CheckpointStore store;
    private final ReconciliationGate gate;
    private final CutoverAction cutoverAction;

    // 可选依赖（Builder 中全部解为实际值）
    private final RegionClientRegistry registry;
    private final MigrationProperties properties;
    private final MigrationNotifier notifier;
    private final TokenBucketRateLimiter rateLimiter;
    private final RetryStrategy retryStrategy;

    // 从 Properties 解出的一次性参数
    private final int batchSize;
    private final int threads;
    private final long tenantTimeoutMinutes;

    private MigrationEngine(Builder builder) {
        this.store = builder.store;
        this.gate = builder.gate;
        this.cutoverAction = builder.cutoverAction;
        this.registry = builder.registry;
        this.properties = builder.properties;
        this.notifier = builder.notifier;
        this.rateLimiter = builder.rateLimiter;
        this.retryStrategy = builder.retryStrategy;
        this.batchSize = builder.batchSize;
        this.threads = builder.threads;
        this.tenantTimeoutMinutes = builder.tenantTimeoutMinutes;
    }

    // ── 公共 API ──

    /**
     * 执行完整迁移：创建 run → 分批并发搬运 → 总量闸门 → 切流。
     *
     * @return runId
     */
    public String migrate(TenantMigrationTask task, MigrationRequest request) {
        String runId = generateRunId(task);
        MigrationRun run = buildRun(runId, request, Direction.FORWARD);
        store.createRun(run, request.getTenantIds());

        MigrationContext ctx = buildContext(request.getSourceRegion(), request.getTargetRegion());
        // 尊重 request 携带的 batchSize/threads（命令行 --batch-size/--threads），
        // 回退到引擎全局配置（migration.default-batch-size/default-threads）
        int effBatch = request.getBatchSize() > 0 ? request.getBatchSize() : batchSize;
        int effThreads = request.getThreads() > 0 ? request.getThreads() : threads;
        TenantBatcher batcher = new TenantBatcher(effBatch, effThreads, tenantTimeoutMinutes);
        batcher.processBatchesConcurrently(request.getTenantIds(), batch ->
                migrateBatch(task, ctx, runId, batch, request.getProduct(), request.getBizLine()));

        finalizeAfterMigration(run, ctx, request.getProduct(), request.getBizLine());
        return runId;
    }

    /**
     * 从断点续传：读取已有 run，处理 PENDING 与 RUNNING 租户。
     *
     * <p>RUNNING 也被重做——这是孤儿租户恢复（CONTEXT.md）：JVM 在 migrateSingleTenant
     * 三步翻转中间崩溃会留下 RUNNING 租户，resume 视同 PENDING 重做（依赖业务 migrate 幂等，ADR-0002）。
     *
     * @return runId（与传入相同）
     */
    public String resume(TenantMigrationTask task, String runId) {
        MigrationRun run = store.findRun(runId);
        if (run == null) {
            throw new IllegalArgumentException("run not found: " + runId);
        }

        // 重做 PENDING + 卡住的 RUNNING（孤儿批恢复）
        List<String> todo = new java.util.ArrayList<>();
        todo.addAll(store.findTenantIdsByStatus(runId, TenantStatus.PENDING));
        todo.addAll(store.findTenantIdsByStatus(runId, TenantStatus.RUNNING));
        if (todo.isEmpty()) {
            return runId;
        }

        MigrationContext ctx = buildContext(run.getSourceRegion(), run.getTargetRegion());
        TenantBatcher batcher = new TenantBatcher(batchSize, threads, tenantTimeoutMinutes);
        batcher.processBatchesConcurrently(todo, batch ->
                migrateBatch(task, ctx, runId, batch, run.getProduct(), run.getBizLine()));

        finalizeAfterMigration(run, ctx, run.getProduct(), run.getBizLine());
        return runId;
    }

    /**
     * 回滚：方向无关。读取原正向 run，创建新 ROLLBACK run（source/target 对调），
     * 对原 run 中 DONE 的租户调用同一个 task.migrate。
     *
     * @return 回滚 run 的 runId
     */
    public String rollback(TenantMigrationTask task, String forwardRunId) {
        MigrationRun forwardRun = store.findRun(forwardRunId);
        if (forwardRun == null) {
            throw new IllegalArgumentException("forward run not found: " + forwardRunId);
        }

        List<String> tenantsToRollback = store.findTenantIdsByStatus(forwardRunId, TenantStatus.DONE);
        if (tenantsToRollback.isEmpty()) {
            return forwardRunId;
        }

        String rollbackRunId = task.taskName() + "-rollback-" + UUID.randomUUID().toString().substring(0, 8);
        MigrationRun rollbackRun = buildRollbackRun(rollbackRunId, forwardRun, tenantsToRollback.size());
        store.createRun(rollbackRun, tenantsToRollback);

        MigrationContext ctx = buildContext(rollbackRun.getSourceRegion(), rollbackRun.getTargetRegion());
        TenantBatcher batcher = new TenantBatcher(batchSize, threads, tenantTimeoutMinutes);
        batcher.processBatchesConcurrently(tenantsToRollback, batch ->
                migrateBatch(task, ctx, rollbackRunId, batch,
                        rollbackRun.getProduct(), rollbackRun.getBizLine()));

        finalizeAfterMigration(rollbackRun, ctx, rollbackRun.getProduct(), rollbackRun.getBizLine());
        return rollbackRunId;
    }

    // ── 批处理 ──

    /**
     * 单批迁移：批内所有租户置 RUNNING → 按租户数限流 → 重试 → 批内统一 DONE 或 FAILED（批级隔离）。
     *
     * <p>批是状态原子单元：批 {@code migrate} 抛异常 → 整批所有租户标 FAILED（ADR-0004）。
     * 状态写入本身也可能失败（DB 抖动），用 safeUpdate 兜底，避免状态异常击穿隔离。
     */
    private void migrateBatch(TenantMigrationTask task, MigrationContext ctx,
                              String runId, List<String> batchTenantIds,
                              String product, String bizLine) {
        String firstTenant = batchTenantIds.isEmpty() ? "?" : batchTenantIds.get(0);
        try {
            // 批内所有租户置 RUNNING
            for (String tenantId : batchTenantIds) {
                safeUpdateTenantState(runId, tenantId, TenantStatus.RUNNING, null);
            }
            rateLimiter.acquire(1); // 按批限流：每批 acquire 1 个令牌
            retryStrategy.executeWithRetry(firstTenant, () ->
                    task.migrate(ctx, batchTenantIds, product, bizLine));
            // 批内所有租户置 DONE
            for (String tenantId : batchTenantIds) {
                safeUpdateTenantState(runId, tenantId, TenantStatus.DONE, null);
            }
        } catch (RuntimeException e) {
            log.warn("batch migration failed ({} tenants, first={}): {}",
                    batchTenantIds.size(), firstTenant, e.getMessage());
            // 批级隔离：整批标 FAILED
            for (String tenantId : batchTenantIds) {
                safeUpdateTenantState(runId, tenantId, TenantStatus.FAILED, e.getMessage());
            }
        }
    }

    /** 状态写入的兜底：失败时记日志，不让单租户的状态异常冒泡炸掉批次 */
    private void safeUpdateTenantState(String runId, String tenantId,
                                       TenantStatus status, String errorContext) {
        try {
            store.updateTenantState(runId, tenantId, status, errorContext);
        } catch (RuntimeException e) {
            log.error("failed to persist tenant state: run={}, tenant={}, targetStatus={}",
                    runId, tenantId, status, e);
        }
    }

    // ── 收尾 ──

    /**
     * 搬运后收尾：汇总计数 → 零失败硬规则 → 对账闸门 → 切流或停止。
     *
     * <p>零失败硬规则（决策 #25）：有 FAILED 租户则不切流、Run 标 FAILED。
     * 切流是不可逆动作（已发 Kafka、业务侧已清内存），跳过失败租户切流等于赌"该租户不重要"。
     */
    private void finalizeAfterMigration(MigrationRun run, MigrationContext ctx,
                                        String product, String bizLine) {
        String runId = run.getRunId();

        List<String> done = store.findTenantIdsByStatus(runId, TenantStatus.DONE);
        List<String> failed = store.findTenantIdsByStatus(runId, TenantStatus.FAILED);
        int processed = done.size() + failed.size();
        store.updateRunProgress(runId, processed, failed.size(), RunStatus.RUNNING);

        // 零失败硬规则：有 FAILED 则不切流
        if (!failed.isEmpty()) {
            log.warn("run {} has {} failed tenants, skip cutover; mark run FAILED", runId, failed.size());
            store.updateRunProgress(runId, processed, failed.size(), RunStatus.FAILED);
            return;
        }

        if (gate.check(run, done)) {
            cutoverAction.evict(ctx, done, product, bizLine);
            notifier.notify(run.getSourceRegion(), run.getTargetRegion(),
                    "run=" + runId + ", tenants=" + done.size());
            store.updateRunProgress(runId, processed, failed.size(), RunStatus.DONE);
        } else {
            store.updateRunProgress(runId, processed, failed.size(), RunStatus.FAILED);
        }
    }

    // ── 上下文与实体工厂 ──

    private MigrationContext buildContext(RegionName source, RegionName target) {
        if (registry != null) {
            return new RegistryMigrationContext(source, target, registry, properties);
        }
        // 无 registry：测试/回退场景。client 调用会抛 UnsupportedOperationException。
        return new RegistryMigrationContext(source, target,
                new RegionClientRegistry(), properties);
    }

    private MigrationRun buildRun(String runId, MigrationRequest request, Direction direction) {
        MigrationRun run = new MigrationRun();
        run.setRunId(runId);
        run.setTaskName(request.getTaskName());
        run.setDirection(direction);
        run.setSourceRegion(request.getSourceRegion());
        run.setTargetRegion(request.getTargetRegion());
        run.setProduct(request.getProduct());
        run.setBizLine(request.getBizLine());
        run.setStatus(RunStatus.RUNNING);
        run.setTotalTenants(request.getTenantIds().size());
        run.setStartedAt(LocalDateTime.now());
        run.setUpdatedAt(LocalDateTime.now());
        return run;
    }

    private MigrationRun buildRollbackRun(String runId, MigrationRun forwardRun, int totalTenants) {
        MigrationRun run = new MigrationRun();
        run.setRunId(runId);
        run.setTaskName(forwardRun.getTaskName());
        run.setDirection(Direction.ROLLBACK);
        run.setSourceRegion(forwardRun.getTargetRegion());
        run.setTargetRegion(forwardRun.getSourceRegion());
        run.setProduct(forwardRun.getProduct());
        run.setBizLine(forwardRun.getBizLine());
        run.setStatus(RunStatus.RUNNING);
        run.setTotalTenants(totalTenants);
        run.setStartedAt(LocalDateTime.now());
        run.setUpdatedAt(LocalDateTime.now());
        run.setParentRunId(forwardRun.getRunId());
        return run;
    }

    private String generateRunId(TenantMigrationTask task) {
        return task.taskName() + "-run-" + UUID.randomUUID().toString().substring(0, 8);
    }

    // ── Builder ──

    /** 创建 Builder。三个必需依赖直接传入，其余可选。 */
    public static Builder builder(CheckpointStore store, ReconciliationGate gate, CutoverAction cutoverAction) {
        return new Builder(store, gate, cutoverAction);
    }

    public static class Builder {
        // 必需
        private final CheckpointStore store;
        private final ReconciliationGate gate;
        private final CutoverAction cutoverAction;

        // 可选 → build() 中全部解为非 null
        private RegionClientRegistry registry;
        private MigrationProperties properties;
        private MigrationNotifier notifier = MigrationNotifier.NO_OP;
        private TokenBucketRateLimiter rateLimiter;
        private RetryStrategy retryStrategy;

        // 从 Properties 解出的参数（build() 中设置）
        private int batchSize;
        private int threads;
        private long tenantTimeoutMinutes;

        private Builder(CheckpointStore store, ReconciliationGate gate, CutoverAction cutoverAction) {
            this.store = store;
            this.gate = gate;
            this.cutoverAction = cutoverAction;
        }

        public Builder registry(RegionClientRegistry registry) {
            this.registry = registry;
            return this;
        }

        public Builder properties(MigrationProperties properties) {
            this.properties = properties;
            return this;
        }

        public Builder notifier(MigrationNotifier notifier) {
            this.notifier = notifier;
            return this;
        }

        public Builder rateLimiter(TokenBucketRateLimiter rateLimiter) {
            this.rateLimiter = rateLimiter;
            return this;
        }

        public Builder retryStrategy(RetryStrategy retryStrategy) {
            this.retryStrategy = retryStrategy;
            return this;
        }

        /**
         * 构建 Engine：解析所有默认值，消除引擎内部的 null guard。
         * 构建后 Builder 可丢弃——Engine 是完整的。
         */
        public MigrationEngine build() {
            resolveDefaults();
            return new MigrationEngine(this);
        }

        /** 为未显式设值的可选依赖解析默认值 */
        private void resolveDefaults() {
            if (this.properties == null) {
                this.properties = new MigrationProperties(); // 全部默认值
            }
            if (this.rateLimiter == null) {
                int qps = properties.getRateLimitQps();
                if (qps <= 0) {
                    this.rateLimiter = TokenBucketRateLimiter.noop();
                } else {
                    this.rateLimiter = new TokenBucketRateLimiter(qps, qps); // 容量 = 速率
                }
            }
            if (this.retryStrategy == null) {
                if (properties.getRetry() != null) {
                    this.retryStrategy = new RetryStrategy(
                            properties.getRetry().getMaxAttempts(),
                            parseBackoffMillis(properties.getRetry().getBackoffInitial()));
                } else {
                    this.retryStrategy = RetryStrategy.noRetry();
                }
            }
            this.batchSize = properties.getDefaultBatchSize();
            this.threads = properties.getDefaultThreads();
            this.tenantTimeoutMinutes = properties.getTenantTimeoutMinutes();
        }

        /** 把 "1s" / "500ms" 之类的字符串转为毫秒数（宽松容错） */
        private static long parseBackoffMillis(String backoff) {
            if (backoff == null) return 1000L;
            String s = backoff.trim().toLowerCase();
            try {
                if (s.endsWith("ms")) return Long.parseLong(s.replace("ms", "").trim());
                if (s.endsWith("s")) return (long)(Double.parseDouble(s.replace("s", "").trim()) * 1000);
                return Long.parseLong(s); // 裸数字视为毫秒
            } catch (NumberFormatException e) {
                log.warn("invalid backoff format '{}', using default 1000ms", backoff);
                return 1000L;
            }
        }
    }

    // ── MigrationContext 实现 ──

    /** 真实上下文：通过 RegionClientRegistry 提供客户端（实现见 {@link RegistryMigrationContext}） */
}
