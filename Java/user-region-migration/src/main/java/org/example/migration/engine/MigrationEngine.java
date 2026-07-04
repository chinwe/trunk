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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 迁移引擎——框架内核。编排分批、并发、断点、对账、切流。
 *
 * 通过依赖倒置（CheckpointStore / ReconciliationGate）实现可测性。
 * 支持批次间并发（线程池），单批内串行。
 */
public class MigrationEngine {

    private static final Logger log = LoggerFactory.getLogger(MigrationEngine.class);

    private final CheckpointStore store;
    private final ReconciliationGate gate;
    private final CutoverAction cutoverAction;
    private final RegionClientRegistry registry;
    private final MigrationProperties properties;
    private final MigrationNotifier notifier;

    /** 旧构造器：兼容无 registry/config 的测试场景 */
    public MigrationEngine(CheckpointStore store, ReconciliationGate gate, CutoverAction cutoverAction) {
        this(store, gate, cutoverAction, null, null, MigrationNotifier.NO_OP);
    }

    public MigrationEngine(CheckpointStore store, ReconciliationGate gate, CutoverAction cutoverAction,
                           RegionClientRegistry registry, MigrationProperties properties, MigrationNotifier notifier) {
        this.store = store;
        this.gate = gate;
        this.cutoverAction = cutoverAction;
        this.registry = registry;
        this.properties = properties;
        this.notifier = notifier;
    }

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

        // 分批并发搬运
        processTenantsConcurrently(task, ctx, runId, request.getTenantIds(),
                request.getProduct(), request.getBizLine(), request.getThreads());

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
        processTenantsConcurrently(task, ctx, runId, pending,
                run.getProduct(), run.getBizLine(), resolveThreads());

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
        MigrationRun rollbackRun = new MigrationRun();
        rollbackRun.setRunId(rollbackRunId);
        rollbackRun.setTaskName(forwardRun.getTaskName());
        rollbackRun.setDirection(Direction.ROLLBACK);
        rollbackRun.setSourceRegion(forwardRun.getTargetRegion());
        rollbackRun.setTargetRegion(forwardRun.getSourceRegion());
        rollbackRun.setProduct(forwardRun.getProduct());
        rollbackRun.setBizLine(forwardRun.getBizLine());
        rollbackRun.setStatus(RunStatus.RUNNING);
        rollbackRun.setTotalTenants(tenantsToRollback.size());
        rollbackRun.setStartedAt(LocalDateTime.now());
        rollbackRun.setUpdatedAt(LocalDateTime.now());
        rollbackRun.setParentRunId(forwardRunId);
        store.createRun(rollbackRun, tenantsToRollback);

        MigrationContext ctx = buildContext(rollbackRun.getSourceRegion(), rollbackRun.getTargetRegion());
        processTenantsConcurrently(task, ctx, rollbackRunId, tenantsToRollback,
                rollbackRun.getProduct(), rollbackRun.getBizLine(), resolveThreads());

        finalizeAfterMigration(rollbackRun, ctx, rollbackRun.getProduct(), rollbackRun.getBizLine());

        return rollbackRunId;
    }

    /**
     * 分批并发处理租户。批次间用线程池并发，单批内逐租户串行。
     * 单租户隔离：失败的租户记 FAILED，不影响同批或后续批次。
     */
    private void processTenantsConcurrently(TenantMigrationTask task, MigrationContext ctx, String runId,
                                            List<String> tenantIds, String product, String bizLine, int threads) {
        int batchSize = resolveBatchSize();
        List<List<String>> batches = partition(tenantIds, batchSize);
        int poolSize = Math.max(1, threads);

        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (List<String> batch : batches) {
            futures.add(CompletableFuture.runAsync(() -> {
                for (String tenantId : batch) {
                    migrateSingleTenant(task, ctx, runId, tenantId, product, bizLine);
                }
            }, executor));
        }
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        } catch (Exception e) {
            throw new RuntimeException("migration batch processing interrupted", e);
        } finally {
            shutdownExecutor(executor);
        }
    }

    /** 单租户迁移：try-catch 隔离 + 可选重试 */
    private void migrateSingleTenant(TenantMigrationTask task, MigrationContext ctx,
                                     String runId, String tenantId, String product, String bizLine) {
        try {
            migrateWithRetry(task, ctx, runId, tenantId, product, bizLine);
            store.updateTenantState(runId, tenantId, TenantStatus.DONE, null);
        } catch (RuntimeException e) {
            log.warn("tenant {} migration failed: {}", tenantId, e.getMessage());
            store.updateTenantState(runId, tenantId, TenantStatus.FAILED, e.getMessage());
        }
    }

    /** 带 Spring Retry 的单租户迁移（瞬时异常重试） */
    private void migrateWithRetry(TenantMigrationTask task, MigrationContext ctx,
                                  String runId, String tenantId, String product, String bizLine) {
        int maxAttempts = resolveRetryMaxAttempts();
        RuntimeException lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                task.migrate(ctx, List.of(tenantId), product, bizLine);
                return;
            } catch (RuntimeException e) {
                lastException = e;
                log.debug("tenant {} attempt {} failed: {}", tenantId, attempt, e.getMessage());
                if (attempt < maxAttempts) {
                    sleepBackoff(attempt);
                }
            }
        }
        throw lastException;
    }

    private void sleepBackoff(int attempt) {
        try {
            Thread.sleep(Math.min(1000L * attempt, 5000L));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /** 搬运后收尾：汇总计数 → 总量闸门 → 切流或停止 */
    private void finalizeAfterMigration(MigrationRun run, MigrationContext ctx,
                                        String product, String bizLine) {
        String runId = run.getRunId();

        List<String> done = store.findTenantIdsByStatus(runId, TenantStatus.DONE);
        List<String> failed = store.findTenantIdsByStatus(runId, TenantStatus.FAILED);
        int processed = done.size() + failed.size();
        store.updateRunProgress(runId, processed, failed.size(), RunStatus.RUNNING);

        if (gate.check(run)) {
            cutoverAction.evict(ctx, done, product, bizLine);
            notifier.notify(run.getSourceRegion(), run.getTargetRegion(),
                    "migration-done", "run=" + runId + ", tenants=" + done.size());
            store.updateRunProgress(runId, processed, failed.size(), RunStatus.DONE);
        } else {
            store.updateRunProgress(runId, processed, failed.size(), RunStatus.FAILED);
        }
    }

    private MigrationContext buildContext(RegionName source, RegionName target) {
        if (registry != null) {
            return new RegistryMigrationContext(source, target, registry, properties);
        }
        // 测试回退：无 registry 时返回最小上下文
        return new SimpleMigrationContext(source, target);
    }

    private int resolveBatchSize() {
        return properties != null ? properties.getDefaultBatchSize() : 50;
    }

    private int resolveThreads() {
        return properties != null ? properties.getDefaultThreads() : 1;
    }

    private int resolveRetryMaxAttempts() {
        if (properties != null && properties.getRetry() != null) {
            return properties.getRetry().getMaxAttempts();
        }
        return 1;
    }

    /** 把租户列表按 batchSize 切分 */
    static <T> List<List<T>> partition(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batches;
    }

    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
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

    private String generateRunId(TenantMigrationTask task) {
        return task.taskName() + "-run-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /** 真实上下文：通过 RegionClientRegistry 提供客户端 */
    private record RegistryMigrationContext(RegionName sourceRegion, RegionName targetRegion,
                                            RegionClientRegistry registry, MigrationProperties config)
            implements MigrationContext {

        @Override
        public RegionName sourceRegion() {
            return sourceRegion;
        }

        @Override
        public RegionName targetRegion() {
            return targetRegion;
        }

        @Override
        public <C extends RegionClient> C client(RegionName region, ClientType type, Class<C> clazz) {
            return registry.client(region, type, clazz);
        }

        @Override
        public <C extends RegionClient> C client(RegionName region, ClientType type, String instance, Class<C> clazz) {
            return registry.client(region, type, instance, clazz);
        }

        @Override
        public MigrationProperties config() {
            return config;
        }
    }

    /** 简化上下文：无 registry，仅用于测试 */
    private record SimpleMigrationContext(RegionName sourceRegion, RegionName targetRegion)
            implements MigrationContext {

        @Override
        public RegionName sourceRegion() {
            return sourceRegion;
        }

        @Override
        public RegionName targetRegion() {
            return targetRegion;
        }

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
