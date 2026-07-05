package org.example.migration.engine;

import org.example.migration.config.MigrationProperties;
import org.example.migration.domain.Direction;
import org.example.migration.domain.MigrationPhase;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    /** 构造引擎：闸门、切流、通知器由具体测试注入（验证两阶段双通知） */
    private MigrationEngine buildEngine(ReconciliationGate gate, RecordingCutoverAction cutover,
                                        MigrationNotifier notifier) {
        return MigrationEngine.builder(store, gate, cutover).notifier(notifier).build();
    }

    /** 默认迁移请求：3 个租户新加坡→缅甸 */
    private MigrationRequest request(List<String> tenantIds) {
        return new MigrationRequest("user-migration", RegionName.SINGAPORE, RegionName.MYANMAR,
                "saas-im", "im", tenantIds, 50, 1);
    }

    /** 单租户批（batch=1）：每个租户独立成批，验证批间隔离 */
    private MigrationRequest requestPerTenantBatch(List<String> tenantIds) {
        return new MigrationRequest("user-migration", RegionName.SINGAPORE, RegionName.MYANMAR,
                "saas-im", "im", tenantIds, 1, 1);
    }

    @Nested
    @DisplayName("批级隔离（ADR-0004）")
    class BatchIsolation {

        @Test
        @DisplayName("批间隔离:某批失败不影响其他批(batch=1,每个租户独立成批)")
        void shouldIsolateFailedBatchFromOthers() {
            // batch=1: t1/t2/t3 各自独立成批
            FakeMigrationTask task = new FakeMigrationTask("user-migration").failOn("t2");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(true), cutover);

            String runId = engine.migrate(task, requestPerTenantBatch(List.of("t1", "t2", "t3")));

            // t1/t3 各自的批成功
            assertThat(store.findTenantIdsByStatus(runId, TenantStatus.DONE))
                    .containsExactlyInAnyOrder("t1", "t3");
            // t2 所在批失败
            List<String> failed = store.findTenantIdsByStatus(runId, TenantStatus.FAILED);
            assertThat(failed).containsExactly("t2");
            // 失败批带 errorContext
            assertThat(store.findTenantStatesByStatus(runId, TenantStatus.FAILED)
                    .getFirst().getErrorContext()).isNotBlank();
            // run 计数正确
            MigrationRun run = store.findRun(runId);
            assertThat(run.getProcessedTenants()).isEqualTo(3);
            assertThat(run.getFailedTenants()).isEqualTo(1);
        }

        @Test
        @DisplayName("批内全失败:批 migrate 抛异常 → 整批所有租户标 FAILED")
        void shouldFailEntireBatchWhenMigrateThrows() {
            // batch=50: t1/t2/t3 同批,其中 t2 触发抛异常 → 整批 FAILED
            FakeMigrationTask task = new FakeMigrationTask("user-migration").failOn("t2");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(true), cutover);

            String runId = engine.migrate(task, request(List.of("t1", "t2", "t3")));

            // 整批失败:t1/t2/t3 都标 FAILED(批级隔离语义)
            assertThat(store.findTenantIdsByStatus(runId, TenantStatus.DONE)).isEmpty();
            assertThat(store.findTenantIdsByStatus(runId, TenantStatus.FAILED))
                    .containsExactlyInAnyOrder("t1", "t2", "t3");
            // 失败批带 errorContext
            assertThat(store.findTenantStatesByStatus(runId, TenantStatus.FAILED)
                    .getFirst().getErrorContext()).isNotBlank();
            // run 计数:3 个都计入 failed
            MigrationRun run = store.findRun(runId);
            assertThat(run.getProcessedTenants()).isEqualTo(3);
            assertThat(run.getFailedTenants()).isEqualTo(3);
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

        @Test
        @DisplayName("C3 孤儿租户恢复:resume 重做卡在 RUNNING 的租户(崩溃在 migrateSingleTenant 中间态)")
        void shouldRecoverOrphanRunningTenantsOnResume() {
            // 模拟崩溃:pre-seed 一个 run,t1 已 DONE,t2 卡在 RUNNING(崩溃在 RUNNING→DONE 之间),t3 仍 PENDING
            String runId = "user-migration-run-orphan";
            preSeedRun(runId, List.of("t1", "t2", "t3"));
            store.updateTenantState(runId, "t1", TenantStatus.DONE, null);
            store.updateTenantState(runId, "t2", TenantStatus.RUNNING, null);  // 孤儿:崩溃遗留

            FakeMigrationTask task = new FakeMigrationTask("user-migration");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(true), cutover);

            engine.resume(task, runId);

            // 关键断言:RUNNING 孤儿 t2 与 PENDING 的 t3 都被重做(依赖业务幂等,ADR-0002)
            assertThat(task.getMigratedTenants()).containsExactlyInAnyOrder("t2", "t3");
            // 无 RUNNING/PENDING 残留,全部 DONE
            assertThat(store.findTenantIdsByStatus(runId, TenantStatus.RUNNING)).isEmpty();
            assertThat(store.findTenantIdsByStatus(runId, TenantStatus.PENDING)).isEmpty();
            assertThat(store.findTenantIdsByStatus(runId, TenantStatus.DONE))
                    .containsExactlyInAnyOrder("t1", "t2", "t3");
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

        @Test
        @DisplayName("C2 零失败硬规则:有 FAILED 租户时不切流且 run 标 FAILED,即便闸门会通过")
        void shouldNotCutoverWhenAnyTenantFailedEvenIfGatePasses() {
            // batch=1:t2 单独成批失败,闸门配置为通过(模拟业务自证不严谨)
            FakeMigrationTask task = new FakeMigrationTask("user-migration").failOn("t2");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(true), cutover);

            String runId = engine.migrate(task, requestPerTenantBatch(List.of("t1", "t2", "t3")));

            // 关键断言:即使闸门会通过,有 FAILED 也不切流
            assertThat(cutover.isEvictCalled()).isFalse();
            assertThat(store.findRun(runId).getStatus()).isEqualTo(RunStatus.FAILED);
            // 失败批记录在案,可被后续 resume 重试(依赖业务幂等,ADR-0002)
            assertThat(store.findTenantIdsByStatus(runId, TenantStatus.FAILED)).containsExactly("t2");
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
        @DisplayName("回滚:方向无关,框架对调 source/target 后复用同一 task.migrate（仅未切流态允许）")
        void shouldRollbackByReversingDirection() {
            // 模拟 CORE 阶段失败（未切流）：pre-seed 一个 RUNNING_CORE run，t1/t2 已 DONE
            // ADR-0005 Q6：已切流态（CORE_CUTOVER_DONE/RUNNING_SECONDARY/DONE）禁 rollback
            String forwardRunId = "user-migration-run-rollback-base";
            preSeedRun(forwardRunId, List.of("t1", "t2"));
            store.updateTenantState(forwardRunId, "t1", TenantStatus.DONE, null);
            store.updateTenantState(forwardRunId, "t2", TenantStatus.DONE, null);

            FakeMigrationTask task = new FakeMigrationTask("user-migration");
            RecordingCutoverAction rollbackCutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(true), rollbackCutover);

            String rollbackRunId = engine.rollback(task, forwardRunId);

            // 回滚 run 方向为 ROLLBACK，source/target 对调
            MigrationRun rollbackRun = store.findRun(rollbackRunId);
            assertThat(rollbackRun.getDirection()).isEqualTo(Direction.ROLLBACK);
            assertThat(rollbackRun.getSourceRegion()).isEqualTo(RegionName.MYANMAR);
            assertThat(rollbackRun.getTargetRegion()).isEqualTo(RegionName.SINGAPORE);
            // 回滚对原 DONE 租户再次调用 migrate（CORE 阶段，业务内反向执行）
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
            // batch=1:每个租户独立成批,失败只影响单租户批(批间隔离)
            FakeMigrationTask task = new FakeMigrationTask("user-migration");
            task.failOn("t5").failOn("t15");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(true), cutover);

            List<String> tenants = java.util.stream.Stream
                    .iterate(1, i -> i + 1).limit(20).map(i -> "t" + i).toList();
            String runId = engine.migrate(task, new org.example.migration.engine.MigrationRequest(
                    "user-migration", RegionName.SINGAPORE, RegionName.MYANMAR,
                    "p", "b", tenants, 1, 4));

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
                        MigrationContext ctx, List<String> tenantIds, String product, String bizLine,
                        org.example.migration.domain.MigrationPhase phase) {
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

    @Nested
    @DisplayName("两阶段迁移（ADR-0005）")
    class TwoPhaseMigration {

        @Test
        @DisplayName("CORE 全部完成后才进 SECONDARY,两阶段顺序执行")
        void shouldRunCoreThenSecondaryInOrder() {
            FakeMigrationTask task = new FakeMigrationTask("user-migration");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(true), cutover);

            String runId = engine.migrate(task, request(List.of("t1", "t2")));

            // CORE 与 SECONDARY 都对全部租户调用了一次
            assertThat(task.getMigratedTenants()).containsExactlyInAnyOrder("t1", "t2");
            assertThat(task.getSecondaryMigratedTenants()).containsExactlyInAnyOrder("t1", "t2");
            // 终态 DONE
            assertThat(store.findRun(runId).getStatus()).isEqualTo(RunStatus.DONE);
            assertThat(store.findRun(runId).getPhase()).isEqualTo(MigrationPhase.SECONDARY);
        }

        @Test
        @DisplayName("CORE 对账通过后切流 + 状态转 CORE_CUTOVER_DONE（中间态对外可见）")
        void shouldCutoverAfterCoreReconcilePass() {
            FakeMigrationTask task = new FakeMigrationTask("user-migration");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            // 用一个能观察 CORE_CUTOVER_DONE 中间态的方式：CORE 失败让 run 停在中间态不易，
            // 这里改用 pre-seed CORE_CUTOVER_DONE 验证状态可见性 + resume 进入 SECONDARY
            String runId = "user-migration-run-cutover-done";
            preSeedCutoverDoneRun(runId, List.of("t1", "t2"));

            engine = buildEngine(new FakeReconciliationGate(true), cutover);
            engine.resume(task, runId);

            // resume 从 CORE_CUTOVER_DONE 进入 SECONDARY 并完成
            assertThat(task.getSecondaryMigratedTenants()).containsExactlyInAnyOrder("t1", "t2");
            assertThat(store.findRun(runId).getStatus()).isEqualTo(RunStatus.DONE);
        }

        @Test
        @DisplayName("两次 Kafka 通知,payload 含 phase 字段（CORE_CUTOVER / ALL_DONE）")
        void shouldNotifyTwiceWithPhase() {
            FakeMigrationTask task = new FakeMigrationTask("user-migration");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            RecordingMigrationNotifier notifier = new RecordingMigrationNotifier();
            engine = buildEngine(new FakeReconciliationGate(true), cutover, notifier);

            engine.migrate(task, request(List.of("t1", "t2")));

            assertThat(notifier.getPayloads()).hasSize(2);
            assertThat(notifier.getPayloads().get(0)).contains("phase=CORE_CUTOVER");
            assertThat(notifier.getPayloads().get(1)).contains("phase=ALL_DONE");
        }

        @Test
        @DisplayName("CORE 有 FAILED 租户时不切流、不进 SECONDARY,run 标 FAILED")
        void shouldNotEnterSecondaryIfCoreHasFailed() {
            FakeMigrationTask task = new FakeMigrationTask("user-migration").failOn("t2");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            RecordingMigrationNotifier notifier = new RecordingMigrationNotifier();
            engine = buildEngine(new FakeReconciliationGate(true), cutover, notifier);

            String runId = engine.migrate(task, requestPerTenantBatch(List.of("t1", "t2", "t3")));

            // CORE 失败：未切流、未通知、未进 SECONDARY
            assertThat(cutover.isEvictCalled()).isFalse();
            assertThat(notifier.getPayloads()).isEmpty();
            assertThat(task.getSecondaryMigratedTenants()).isEmpty();
            assertThat(store.findRun(runId).getStatus()).isEqualTo(RunStatus.FAILED);
        }

        @Test
        @DisplayName("SECONDARY 对账失败 → run FAILED（已切流,不自动 rollback）")
        void shouldFailRunIfSecondaryReconcileFails() {
            // 先正常完成 CORE 切流（pre-seed CORE_CUTOVER_DONE），SECONDARY 用不通过闸门
            String runId = "user-migration-run-secondary-fail";
            preSeedCutoverDoneRun(runId, List.of("t1", "t2"));

            FakeMigrationTask task = new FakeMigrationTask("user-migration");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(false), cutover); // SECONDARY 对账不通过

            engine.resume(task, runId);

            // SECONDARY 跑完但对账失败 → FAILED
            assertThat(store.findRun(runId).getStatus()).isEqualTo(RunStatus.FAILED);
            assertThat(task.getSecondaryMigratedTenants()).containsExactlyInAnyOrder("t1", "t2");
        }

        @Test
        @DisplayName("已切流态（CORE_CUTOVER_DONE/RUNNING_SECONDARY/DONE）禁 rollback")
        void shouldRejectRollbackAfterCutover() {
            // 三个已切流态都应拒绝
            for (RunStatus terminal : new RunStatus[]{
                    RunStatus.CORE_CUTOVER_DONE, RunStatus.RUNNING_SECONDARY, RunStatus.DONE}) {
                String runId = "user-migration-run-reject-" + terminal.name();
                preSeedRun(runId, List.of("t1"));
                store.updateTenantState(runId, "t1", TenantStatus.DONE, null);
                // 直接把 status 改成目标态（preSeedRun 默认 RUNNING_CORE）
                MigrationRun run = store.findRun(runId);
                run.setStatus(terminal);
                // 通过 updateRunProgress 翻状态
                store.updateRunProgress(runId, 1, 0, terminal);

                FakeMigrationTask task = new FakeMigrationTask("user-migration");
                RecordingCutoverAction cutover = new RecordingCutoverAction();
                engine = buildEngine(new FakeReconciliationGate(true), cutover);

                assertThatThrownBy(() -> engine.rollback(task, runId))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("cannot rollback after cutover");
            }
        }

        @Test
        @DisplayName("resume 在 RUNNING_CORE 态重做 CORE 未完成批")
        void shouldResumeCorePhaseFromUnfinishedBatches() {
            String runId = "user-migration-run-resume-core";
            preSeedRun(runId, List.of("t1", "t2", "t3"));
            store.updateTenantState(runId, "t1", TenantStatus.DONE, null);
            store.updateTenantState(runId, "t2", TenantStatus.DONE, null);
            // t3 仍 PENDING

            FakeMigrationTask task = new FakeMigrationTask("user-migration");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(true), cutover);

            engine.resume(task, runId);

            // resume 完成 CORE（t3）→ 切流 → 进 SECONDARY → 完成 SECONDARY
            assertThat(task.getMigratedTenants()).contains("t3");
            assertThat(task.getSecondaryMigratedTenants()).containsExactlyInAnyOrder("t1", "t2", "t3");
            assertThat(store.findRun(runId).getStatus()).isEqualTo(RunStatus.DONE);
        }

        @Test
        @DisplayName("resume 在 CORE_CUTOVER_DONE 态跳过 CORE,直接进 SECONDARY")
        void shouldResumeFromCutoverDoneToSecondary() {
            String runId = "user-migration-run-resume-cutover";
            preSeedCutoverDoneRun(runId, List.of("t1", "t2"));

            FakeMigrationTask task = new FakeMigrationTask("user-migration");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(true), cutover);

            engine.resume(task, runId);

            // CORE 不重做（CORE 集合空），SECONDARY 跑全部
            assertThat(task.getMigratedTenants()).isEmpty();
            assertThat(task.getSecondaryMigratedTenants()).containsExactlyInAnyOrder("t1", "t2");
            assertThat(store.findRun(runId).getStatus()).isEqualTo(RunStatus.DONE);
        }

        @Test
        @DisplayName("resume 在 RUNNING_SECONDARY 态重做 SECONDARY 未完成批")
        void shouldResumeSecondaryPhaseFromUnfinishedBatches() {
            String runId = "user-migration-run-resume-secondary";
            preSeedCutoverDoneRun(runId, List.of("t1", "t2"));
            // 模拟 SECONDARY 已跑了一半：t1 DONE，t2 仍 PENDING（CORE_CUTOVER_DONE → resume 重置后 t1 又被改 DONE）
            // 但 preSeedCutoverDoneRun 之后租户都是 DONE；resume 会先重置为 PENDING 再跑 SECONDARY
            // 这里直接构造 RUNNING_SECONDARY + 部分 PENDING 来测 resume 的 SECONDARY 分支
            store.updateRunProgress(runId, 1, 0, RunStatus.RUNNING_SECONDARY);
            store.updateTenantState(runId, "t1", TenantStatus.DONE, null);
            store.updateTenantState(runId, "t2", TenantStatus.PENDING, null);

            FakeMigrationTask task = new FakeMigrationTask("user-migration");
            RecordingCutoverAction cutover = new RecordingCutoverAction();
            engine = buildEngine(new FakeReconciliationGate(true), cutover);

            engine.resume(task, runId);

            // SECONDARY resume 只重做 t2
            assertThat(task.getSecondaryMigratedTenants()).containsExactly("t2");
            assertThat(store.findRun(runId).getStatus()).isEqualTo(RunStatus.DONE);
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
        run.setStatus(RunStatus.RUNNING_CORE);
        run.setPhase(MigrationPhase.CORE);
        run.setTotalTenants(tenantIds.size());
        store.createRun(run, tenantIds);
    }

    /**
     * 预置一个 CORE 已切流的 run（CORE_CUTOVER_DONE 中间态）。
     * 模拟 CORE 阶段完成切流、尚未进入 SECONDARY 即崩溃的场景。
     * 租户全部 DONE（CORE 已搬完），等待 resume 进入 SECONDARY。
     */
    private void preSeedCutoverDoneRun(String runId, List<String> tenantIds) {
        preSeedRun(runId, tenantIds);
        for (String tenantId : tenantIds) {
            store.updateTenantState(runId, tenantId, TenantStatus.DONE, null);
        }
        store.updateRunProgress(runId, tenantIds.size(), 0, RunStatus.CORE_CUTOVER_DONE);
        MigrationRun run = store.findRun(runId);
        run.setPhase(MigrationPhase.CORE);
        // 内存 store 直接改 phase；Jdbc store 走 updateRunProgress 时不动 phase（phase 由 status 隐式编码）
    }
}
