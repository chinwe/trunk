package org.example.migration.engine;

import org.example.migration.domain.Direction;
import org.example.migration.domain.RegionName;
import org.example.migration.domain.RunStatus;
import org.example.migration.domain.TenantStatus;
import org.example.migration.domain.entity.MigrationRun;
import org.example.migration.domain.entity.MigrationTenantState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CheckpointStore 契约测试。以 InMemoryCheckpointStore 为载体验证 store 的可观察行为。
 * 这些行为规约对 JdbcCheckpointStore 同样成立（生产实现应通过同一套契约测试）。
 */
class InMemoryCheckpointStoreTest {

    private CheckpointStore store = new InMemoryCheckpointStore();

    private MigrationRun newRun(String runId) {
        MigrationRun run = new MigrationRun();
        run.setRunId(runId);
        run.setTaskName("user-migration");
        run.setDirection(Direction.FORWARD);
        run.setSourceRegion(RegionName.SINGAPORE);
        run.setTargetRegion(RegionName.MYANMAR);
        run.setStatus(RunStatus.RUNNING);
        run.setTotalTenants(3);
        return run;
    }

    @Nested
    @DisplayName("创建 run 与租户初始化")
    class CreateRun {

        @Test
        @DisplayName("创建 run 后能按 runId 读回")
        void shouldRetrieveRunByIdAfterCreation() {
            store.createRun(newRun("run-1"), List.of("t1", "t2", "t3"));

            MigrationRun found = store.findRun("run-1");

            assertThat(found).isNotNull();
            assertThat(found.getTaskName()).isEqualTo("user-migration");
            assertThat(found.getDirection()).isEqualTo(Direction.FORWARD);
        }

        @Test
        @DisplayName("创建 run 时所有租户初始化为 PENDING")
        void shouldInitializeAllTenantsAsPending() {
            store.createRun(newRun("run-1"), List.of("t1", "t2", "t3"));

            List<String> pending = store.findTenantIdsByStatus("run-1", TenantStatus.PENDING);

            assertThat(pending).containsExactlyInAnyOrder("t1", "t2", "t3");
        }

        @Test
        @DisplayName("查询不存在的 runId 返回 null")
        void shouldReturnNullForUnknownRun() {
            assertThat(store.findRun("nonexistent")).isNull();
        }
    }

    @Nested
    @DisplayName("租户状态翻转")
    class TenantStateTransition {

        @Test
        @DisplayName("租户从 PENDING 翻转为 DONE 后,不再出现在 PENDING 列表")
        void shouldMoveTenantOutOfPendingWhenDone() {
            store.createRun(newRun("run-1"), List.of("t1", "t2"));

            store.updateTenantState("run-1", "t1", TenantStatus.DONE, null);

            assertThat(store.findTenantIdsByStatus("run-1", TenantStatus.PENDING)).containsExactly("t2");
            assertThat(store.findTenantIdsByStatus("run-1", TenantStatus.DONE)).containsExactly("t1");
        }

        @Test
        @DisplayName("FAILED 租户记录 errorContext,便于事后诊断")
        void shouldRecordErrorContextForFailedTenant() {
            store.createRun(newRun("run-1"), List.of("t1"));

            store.updateTenantState("run-1", "t1", TenantStatus.FAILED, "connection timeout to myanmar mysql");

            List<MigrationTenantState> failed = store.findTenantStatesByStatus("run-1", TenantStatus.FAILED);
            assertThat(failed).hasSize(1);
            assertThat(failed.getFirst().getTenantId()).isEqualTo("t1");
            assertThat(failed.getFirst().getErrorContext()).contains("timeout");
        }
    }

    @Nested
    @DisplayName("按状态查询租户(回滚与汇总用)")
    class QueryByStatus {

        @Test
        @DisplayName("查询所有 DONE 租户,回滚时只处理这些")
        void shouldReturnOnlyDoneTenants() {
            store.createRun(newRun("run-1"), List.of("t1", "t2", "t3"));
            store.updateTenantState("run-1", "t1", TenantStatus.DONE, null);
            store.updateTenantState("run-1", "t2", TenantStatus.FAILED, "err");
            // t3 仍 PENDING

            List<String> done = store.findTenantIdsByStatus("run-1", TenantStatus.DONE);

            assertThat(done).containsExactly("t1");
        }

        @Test
        @DisplayName("查询所有 FAILED 租户,迁移结束汇总失败清单")
        void shouldReturnOnlyFailedTenants() {
            store.createRun(newRun("run-1"), List.of("t1", "t2"));
            store.updateTenantState("run-1", "t1", TenantStatus.DONE, null);
            store.updateTenantState("run-1", "t2", TenantStatus.FAILED, "bad data");

            List<String> failed = store.findTenantIdsByStatus("run-1", TenantStatus.FAILED);

            assertThat(failed).containsExactly("t2");
        }
    }

    @Nested
    @DisplayName("run 进度更新")
    class RunProgress {

        @Test
        @DisplayName("更新 run 的计数与状态后,重新读取能反映新值")
        void shouldPersistProgressAndStatus() {
            store.createRun(newRun("run-1"), List.of("t1", "t2", "t3"));

            store.updateRunProgress("run-1", 3, 1, RunStatus.DONE);

            MigrationRun found = store.findRun("run-1");
            assertThat(found.getProcessedTenants()).isEqualTo(3);
            assertThat(found.getFailedTenants()).isEqualTo(1);
            assertThat(found.getStatus()).isEqualTo(RunStatus.DONE);
        }
    }
}
