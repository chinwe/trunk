package org.example.migration.engine;

import org.example.migration.client.RegionClient;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 迁移引擎——框架内核。编排分批、并发、断点、对账、切流。
 *
 * 通过依赖倒置（CheckpointStore / ReconciliationGate）实现可测性，
 * 引擎本身不依赖真实中间件，测试用 fake 驱动。
 */
public class MigrationEngine {

    private final CheckpointStore store;
    private final ReconciliationGate gate;
    private final CutoverAction cutoverAction;

    public MigrationEngine(CheckpointStore store, ReconciliationGate gate, CutoverAction cutoverAction) {
        this.store = store;
        this.gate = gate;
        this.cutoverAction = cutoverAction;
    }

    /**
     * 执行完整迁移：创建 run → 分批搬运 → 总量闸门 → 切流。
     *
     * @return runId
     */
    public String migrate(TenantMigrationTask task, MigrationRequest request) {
        String runId = generateRunId(task);
        MigrationRun run = buildRun(runId, request, Direction.FORWARD);
        store.createRun(run, request.getTenantIds());

        MigrationContext ctx = buildContext(request);

        // 分批搬运：当前实现单批内逐租户处理（并发留待后续）
        for (String tenantId : request.getTenantIds()) {
            migrateSingleTenant(task, ctx, runId, tenantId, request.getProduct(), request.getBizLine());
        }

        finalizeAfterMigration(run, ctx, request.getProduct(), request.getBizLine());

        return runId;
    }

    /**
     * 回滚：方向无关。读取原正向 run，创建新 ROLLBACK run（source/target 对调），
     * 对原 run 中 DONE 的租户调用同一个 task.migrate。
     *
     * 业务 migrate 实现基于 ctx.sourceRegion()/targetRegion()，
     * 回滚时框架对调注入，业务"从 source 读→写 target→删 source"逻辑天然反向执行。
     *
     * @return 回滚 run 的 runId
     */
    public String rollback(TenantMigrationTask task, String forwardRunId) {
        MigrationRun forwardRun = store.findRun(forwardRunId);
        if (forwardRun == null) {
            throw new IllegalArgumentException("forward run not found: " + forwardRunId);
        }

        // 回滚只处理原 run 中已迁移(DONE)的租户
        List<String> tenantsToRollback = store.findTenantIdsByStatus(forwardRunId, TenantStatus.DONE);
        if (tenantsToRollback.isEmpty()) {
            return forwardRunId;
        }

        // 创建回滚 run：source/target 对调，方向 ROLLBACK
        String rollbackRunId = task.taskName() + "-rollback-" + java.util.UUID.randomUUID().toString().substring(0, 8);
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

        MigrationContext ctx = new SimpleMigrationContext(rollbackRun.getSourceRegion(), rollbackRun.getTargetRegion());

        // 复用 migrate 逐租户处理（单租户隔离、状态翻转）
        for (String tenantId : tenantsToRollback) {
            migrateSingleTenant(task, ctx, rollbackRunId, tenantId,
                    rollbackRun.getProduct(), rollbackRun.getBizLine());
        }

        finalizeAfterMigration(rollbackRun, ctx, rollbackRun.getProduct(), rollbackRun.getBizLine());

        return rollbackRunId;
    }

    /**
     * 从断点续传：读取已有 run，只处理 PENDING 租户。
     * 适用于迁移中途崩溃后恢复——已完成的租户不重复处理。
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

        MigrationContext ctx = new SimpleMigrationContext(run.getSourceRegion(), run.getTargetRegion());

        // 只处理 PENDING 租户
        for (String tenantId : pending) {
            migrateSingleTenant(task, ctx, runId, tenantId, run.getProduct(), run.getBizLine());
        }

        // 汇总计数 + 闸门 + 切流（复用正向流程的收尾）
        finalizeAfterMigration(run, ctx, run.getProduct(), run.getBizLine());

        return runId;
    }

    /** 单租户迁移：try-catch 隔离，失败记 FAILED + errorContext */
    private void migrateSingleTenant(TenantMigrationTask task, MigrationContext ctx,
                                     String runId, String tenantId, String product, String bizLine) {
        try {
            task.migrate(ctx, List.of(tenantId), product, bizLine);
            store.updateTenantState(runId, tenantId, TenantStatus.DONE, null);
        } catch (RuntimeException e) {
            store.updateTenantState(runId, tenantId, TenantStatus.FAILED, e.getMessage());
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

        // 总量对账闸门
        if (gate.check(run)) {
            // 闸门通过 → 切流
            cutoverAction.evict(ctx, done, product, bizLine);
            store.updateRunProgress(runId, processed, failed.size(), RunStatus.DONE);
        } else {
            // 闸门不通过 → 停下不切流
            store.updateRunProgress(runId, processed, failed.size(), RunStatus.FAILED);
        }
    }

    private MigrationContext buildContext(MigrationRequest request) {
        // 骨架实现：客户端注册表与配置在后续接入；当前测试不依赖客户端
        return new SimpleMigrationContext(request.getSourceRegion(), request.getTargetRegion());
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

    /** 简化上下文：仅含 region，客户端注册表后续接入 */
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
            throw new UnsupportedOperationException("client registry not wired yet");
        }

        @Override
        public MigrationProperties config() {
            throw new UnsupportedOperationException("config not wired yet");
        }
    }
}
