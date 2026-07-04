package org.example.migration.engine;

import org.example.migration.config.MigrationProperties;
import org.example.migration.domain.Direction;
import org.example.migration.spi.TenantMigrationTask;
import org.example.migration.domain.RegionName;
import org.example.migration.domain.RunStatus;
import org.example.migration.domain.TenantStatus;
import org.example.migration.domain.entity.MigrationRun;
import org.example.migration.spi.MigrationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MigrationEngine 行为测试。框架内核的规约。
 *
 * 测试策略：用 fake SPI + 内存 store + fake 闸门，验证引擎的可观察行为
 * （状态翻转、计数、是否切流），不依赖真实中间件，不 mock 内部。
 */
class MigrationEngineTest {

    private CheckpointStore store;
    private MigrationEngine engine;

    @BeforeEach
    void setUp() {
        store = new InMemoryCheckpointStore();
    }

    /** 构造引擎：闸门与切流动作由具体测试注入，其余依赖走 Builder 默认值 */
    private MigrationEngine buildEngine(ReconciliationGate gate, RecordingCutoverAction cutover) {
        return MigrationEngine.builder(store, gate, cutover).build();
    }

    /** 默认迁移请求：3 个租户新加坡→缅甸 */
    private MigrationRequest request(List<String> tenantIds) {
        return new MigrationRequest("user-migration", RegionName.SINGAPORE, RegionName.MYANMAR,
                "saas-im", "im", tenantIds, 50, 1);
    }

    @Nested
    @DisplayName("单租户隔离")
    class TenantIsolation {

        @Test
        @DisplayName("一批中某租户失败,其余租户正常完成,失败租户记 FAILED 带 errorContext")
        void shouldIsolateFailedTenantAndContinueOthers() {
            FakeMigrationTask task = new FakeMigrationTask("user-migration").failOn("t2");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(true), cutover);

            String runId = engine.migrate(task, request(List.of("t1", "t2", "t3")));

            // 成功租户状态 DONE
            assertThat(store.findTenantIdsByStatus(runId, TenantStatus.DONE))
                    .containsExactlyInAnyOrder("t1", "t3");
            // 失败租户状态 FAILED
            List<String> failed = store.findTenantIdsByStatus(runId, TenantStatus.FAILED);
            assertThat(failed).containsExactly("t2");

            // 失败租户带 errorContext
            assertThat(store.findTenantStatesByStatus(runId, TenantStatus.FAILED)
                    .getFirst().getErrorContext()).isNotBlank();

            // run 计数正确
            MigrationRun run = store.findRun(runId);
            assertThat(run.getProcessedTenants()).isEqualTo(3);
            assertThat(run.getFailedTenants()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("租户级断点续传")
    class ResumeFromCheckpoint {

        @Test
        @DisplayName("resume 只处理 PENDING 租户,已完成的租户不重复迁移")
        void shouldResumeOnlyPendingTenants() {
            // 模拟中途崩溃：pre-seed 一个 run,t1/t2 已 DONE,t3 仍 PENDING
            String runId = "user-migration-run-preset";
            preSeedRun(runId, List.of("t1", "t2", "t3"));
            store.updateTenantState(runId, "t1", TenantStatus.DONE, null);
            store.updateTenantState(runId, "t2", TenantStatus.DONE, null);

            FakeMigrationTask task = new FakeMigrationTask("user-migration");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(true), cutover);

            engine.resume(task, runId);

            // 只 t3 被 task.migrate 处理
            assertThat(task.getMigratedTenants()).containsExactly("t3");
            // t1/t2 仍 DONE（未被重复处理）
            assertThat(store.findTenantIdsByStatus(runId, TenantStatus.DONE))
                    .containsExactlyInAnyOrder("t1", "t2", "t3");
            // 无 PENDING 残留
            assertThat(store.findTenantIdsByStatus(runId, TenantStatus.PENDING)).isEmpty();
        }
    }

    @Nested
    @DisplayName("切流总量闸门")
    class ReconciliationGateControl {

        @Test
        @DisplayName("闸门不通过时,不执行切流,run 状态为 FAILED")
        void shouldNotCutoverWhenGateFails() {
            FakeMigrationTask task = new FakeMigrationTask("user-migration");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(false), cutover);

            String runId = engine.migrate(task, request(List.of("t1", "t2")));

            assertThat(cutover.isEvictCalled()).isFalse();
            assertThat(store.findRun(runId).getStatus()).isEqualTo(RunStatus.FAILED);
        }

        @Test
        @DisplayName("闸门通过时,执行切流并通知全部已迁移租户,run 状态为 DONE")
        void shouldCutoverWhenGatePasses() {
            FakeMigrationTask task = new FakeMigrationTask("user-migration");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(true), cutover);

            String runId = engine.migrate(task, request(List.of("t1", "t2")));

            assertThat(cutover.isEvictCalled()).isTrue();
            assertThat(cutover.getEvictedTenants()).containsExactlyInAnyOrder("t1", "t2");
            assertThat(store.findRun(runId).getStatus()).isEqualTo(RunStatus.DONE);
        }
    }

    @Nested
    @DisplayName("全流程编排")
    class FullFlow {

        @Test
        @DisplayName("完整迁移:全部租户搬运→闸门通过→切流通知全部租户→run DONE")
        void shouldCompleteFullMigrationFlow() {
            FakeMigrationTask task = new FakeMigrationTask("user-migration");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(true), cutover);

            String runId = engine.migrate(task, request(List.of("t1", "t2", "t3")));

            // 所有租户被 task 处理且状态 DONE
            assertThat(task.getMigratedTenants()).containsExactlyInAnyOrder("t1", "t2", "t3");
            assertThat(store.findTenantIdsByStatus(runId, TenantStatus.DONE))
                    .containsExactlyInAnyOrder("t1", "t2", "t3");
            assertThat(store.findTenantIdsByStatus(runId, TenantStatus.PENDING)).isEmpty();
            assertThat(store.findTenantIdsByStatus(runId, TenantStatus.FAILED)).isEmpty();
            // 切流收到全部租户
            assertThat(cutover.getEvictedTenants()).containsExactlyInAnyOrder("t1", "t2", "t3");
            // run 完成
            MigrationRun run = store.findRun(runId);
            assertThat(run.getStatus()).isEqualTo(RunStatus.DONE);
            assertThat(run.getProcessedTenants()).isEqualTo(3);
            assertThat(run.getFailedTenants()).isZero();
        }

        @Test
        @DisplayName("回滚:方向无关,框架对调 source/target 后复用同一 task.migrate")
        void shouldRollbackByReversingDirection() {
            // 先正向迁移完成
            FakeMigrationTask task = new FakeMigrationTask("user-migration");
            RecordingCutoverAction forwardCutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(true), forwardCutover);
            String forwardRunId = engine.migrate(task, request(List.of("t1", "t2")));

            // 清空 task 记录,准备观察回滚调用
            task.getMigratedTenants().clear();

            // 回滚：source/target 对调（缅甸→新加坡）
            RecordingCutoverAction rollbackCutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(true), rollbackCutover);

            String rollbackRunId = engine.rollback(task, forwardRunId);

            // 回滚 run 方向为 ROLLBACK，source/target 对调
            MigrationRun rollbackRun = store.findRun(rollbackRunId);
            assertThat(rollbackRun.getDirection()).isEqualTo(Direction.ROLLBACK);
            assertThat(rollbackRun.getSourceRegion()).isEqualTo(RegionName.MYANMAR);
            assertThat(rollbackRun.getTargetRegion()).isEqualTo(RegionName.SINGAPORE);
            // 回滚对原 DONE 租户再次调用 migrate（业务内反向执行）
            assertThat(task.getMigratedTenants()).containsExactlyInAnyOrder("t1", "t2");
        }
    }

    @Nested
    @DisplayName("并发与重试")
    class ConcurrencyAndRetry {

        @Test
        @DisplayName("多线程并发时,所有租户仍被处理且单租户隔离成立")
        void shouldHandleConcurrentBatchesWithIsolation() {
            // 20 个租户,其中 2 个失败,4 线程并发
            FakeMigrationTask task = new FakeMigrationTask("user-migration");
            task.failOn("t5").failOn("t15");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(true), cutover);

            List<String> tenants = java.util.stream.Stream
                    .iterate(1, i -> i + 1).limit(20).map(i -> "t" + i).toList();
            String runId = engine.migrate(task, new org.example.migration.engine.MigrationRequest(
                    "user-migration", RegionName.SINGAPORE, RegionName.MYANMAR,
                    "p", "b", tenants, 5, 4));

            assertThat(store.findTenantIdsByStatus(runId, TenantStatus.DONE)).hasSize(18);
            assertThat(store.findTenantIdsByStatus(runId, TenantStatus.FAILED))
                    .containsExactlyInAnyOrder("t5", "t15");
            MigrationRun run = store.findRun(runId);
            assertThat(run.getProcessedTenants()).isEqualTo(20);
            assertThat(run.getFailedTenants()).isEqualTo(2);
        }

        @Test
        @DisplayName("瞬时性异常会重试,最终成功则租户状态 DONE")
        void shouldRetryTransientFailure() {
            // 失败一次后成功的 fake 任务
            TenantMigrationTask retryTask = new TenantMigrationTask() {
                final java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();

                @Override
                public String taskName() {
                    return "retry-task";
                }

                @Override
                public org.example.migration.spi.result.MigrationResult migrate(
                        MigrationContext ctx, List<String> tenantIds, String product, String bizLine) {
                    if (calls.incrementAndGet() == 1) {
                        throw new RuntimeException("transient connection timeout");
                    }
                    return org.example.migration.spi.result.MigrationResult.success(tenantIds.size());
                }
            };

            // 配置重试 3 次
            MigrationProperties props = new MigrationProperties();
            MigrationProperties.RetryConfig retryConfig = new MigrationProperties.RetryConfig();
            retryConfig.setMaxAttempts(3);
            retryConfig.setBackoffInitial("100ms");
            props.setRetry(retryConfig);

            store = new InMemoryCheckpointStore();
            engine = MigrationEngine.builder(store, new FakeReconciliationGate(true),
                    new RecordingCutoverAction())
                    .properties(props)
                    .build();

            String runId = engine.migrate(retryTask, request(List.of("t1")));

            assertThat(store.findTenantIdsByStatus(runId, TenantStatus.DONE)).containsExactly("t1");
        }
    }

    /** 预置一个 run（模拟迁移中途崩溃后已持久化的状态） */
    private void preSeedRun(String runId, List<String> tenantIds) {
        MigrationRun run = new MigrationRun();
        run.setRunId(runId);
        run.setTaskName("user-migration");
        run.setDirection(Direction.FORWARD);
        run.setSourceRegion(RegionName.SINGAPORE);
        run.setTargetRegion(RegionName.MYANMAR);
        run.setProduct("saas-im");
        run.setBizLine("im");
        run.setStatus(RunStatus.RUNNING);
        run.setTotalTenants(tenantIds.size());
        store.createRun(run, tenantIds);
    }
}
