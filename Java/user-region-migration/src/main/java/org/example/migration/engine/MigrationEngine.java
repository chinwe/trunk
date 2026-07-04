package org.example.migration.engine;

import org.example.migration.client.RegionClient;
import org.example.migration.client.RegionClientRegistry;
import org.example.migration.config.MigrationProperties;
import org.example.migration.domain.ClientType;
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
        TenantBatcher batcher = new TenantBatcher(batchSize, threads, tenantTimeoutMinutes);
        batcher.processConcurrently(request.getTenantIds(), tenantId ->
                migrateSingleTenant(task, ctx, runId, tenantId, request.getProduct(), request.getBizLine()));

        finalizeAfterMigration(run, ctx, request.getProduct(), request.getBizLine());
        return runId;
    }

    /**
     * 从断点续传：读取已有 run，只处理 PENDING 租户。
     *
     * @return runId（与传入相同）
     */
    public String resume(TenantMigrationTask task, String runId) {
        MigrationRun run = store.findRun(runId);
        if (run == null) {
            throw new IllegalArgumentException("run not found: " + runId);
        }

        List<String> pending = store.findTenantIdsByStatus(runId, TenantStatus.PENDING);
        if (pending.isEmpty()) {
            return runId;
        }

        MigrationContext ctx = buildContext(run.getSourceRegion(), run.getTargetRegion());
        TenantBatcher batcher = new TenantBatcher(batchSize, threads, tenantTimeoutMinutes);
        batcher.processConcurrently(pending, tenantId ->
                migrateSingleTenant(task, ctx, runId, tenantId, run.getProduct(), run.getBizLine()));

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
        batcher.processConcurrently(tenantsToRollback, tenantId ->
                migrateSingleTenant(task, ctx, rollbackRunId, tenantId,
                        rollbackRun.getProduct(), rollbackRun.getBizLine()));

        finalizeAfterMigration(rollbackRun, ctx, rollbackRun.getProduct(), rollbackRun.getBizLine());
        return rollbackRunId;
    }

    // ── 单租户处理 ──

    /** 单租户迁移：限流 → 状态 RUNNING → 重试 → 状态更新（单租户隔离） */
    private void migrateSingleTenant(TenantMigrationTask task, MigrationContext ctx,
                                     String runId, String tenantId, String product, String bizLine) {
        try {
            store.updateTenantState(runId, tenantId, TenantStatus.RUNNING, null);
            rateLimiter.acquire(1); // 全局限流：阻塞等待令牌
            retryStrategy.executeWithRetry(tenantId, () ->
                    task.migrate(ctx, List.of(tenantId), product, bizLine));
            store.updateTenantState(runId, tenantId, TenantStatus.DONE, null);
        } catch (RuntimeException e) {
            log.warn("tenant {} migration failed: {}", tenantId, e.getMessage());
            store.updateTenantState(runId, tenantId, TenantStatus.FAILED, e.getMessage());
        }
    }

    // ── 收尾 ──

    /** 搬运后收尾：汇总计数 → 总量闸门 → 切流或停止 */
    private void finalizeAfterMigration(MigrationRun run, MigrationContext ctx,
                                        String product, String bizLine) {
        String runId = run.getRunId();

        List<String> done = store.findTenantIdsByStatus(runId, TenantStatus.DONE);
        List<String> failed = store.findTenantIdsByStatus(runId, TenantStatus.FAILED);
        int processed = done.size() + failed.size();
        store.updateRunProgress(runId, processed, failed.size(), RunStatus.RUNNING);

        if (gate.check(run, done)) {
            cutoverAction.evict(ctx, done, product, bizLine);
            notifier.notify(run.getSourceRegion(), run.getTargetRegion(),
                    "migration-done", "run=" + runId + ", tenants=" + done.size());
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
        return new SimpleMigrationContext(source, target);
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

    /** 真实上下文：通过 RegionClientRegistry 提供客户端 */
    private record RegistryMigrationContext(RegionName sourceRegion, RegionName targetRegion,
                                            RegionClientRegistry registry, MigrationProperties config)
            implements MigrationContext {

        @Override
        public RegionName sourceRegion() { return sourceRegion; }

        @Override
        public RegionName targetRegion() { return targetRegion; }

        @Override
        public <C extends RegionClient> C client(RegionName region, ClientType type, Class<C> clazz) {
            return registry.client(region, type, clazz);
        }

        @Override
        public <C extends RegionClient> C client(RegionName region, ClientType type, String instance, Class<C> clazz) {
            return registry.client(region, type, instance, clazz);
        }

        @Override
        public MigrationProperties config() { return config; }
    }

    /** 简化上下文：无 registry（测试/回退用） */
    private record SimpleMigrationContext(RegionName sourceRegion, RegionName targetRegion)
            implements MigrationContext {

        @Override
        public RegionName sourceRegion() { return sourceRegion; }

        @Override
        public RegionName targetRegion() { return targetRegion; }

        @Override
        public <C extends RegionClient> C client(RegionName region, ClientType type, Class<C> clazz) {
            throw new UnsupportedOperationException("client registry not wired");
        }

        @Override
        public <C extends RegionClient> C client(RegionName region, ClientType type, String instance, Class<C> clazz) {
            throw new UnsupportedOperationException("client registry not wired");
        }

        @Override
        public MigrationProperties config() {
            throw new UnsupportedOperationException("config not wired");
        }
    }
}
