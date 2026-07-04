package org.example.migration.shell;

import org.example.migration.client.RegionClientRegistry;
import org.example.migration.config.MigrationProperties;
import org.example.migration.domain.TenantStatus;
import org.example.migration.engine.FakeMigrationTask;
import org.example.migration.engine.JdbcCheckpointStore;
import org.example.migration.engine.MigrationNotifier;
import org.example.migration.engine.RecordingCutoverAction;
import org.example.migration.engine.ReconciliationCounter;
import org.example.migration.engine.TenantScanner;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MigrationCommands 测试：用真实 H2 状态库 + 内存 registry + fake task 验证命令端到端。
 * 覆盖 migrate/resume/rollback/verify/status/tasks/dry-run 各命令。
 */
class MigrationCommandsTest {

    private TaskRegistry taskRegistry;
    private DataSource dataSource;
    private MigrationCommands commands;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // H2 状态库
        JdbcDataSource h2 = new JdbcDataSource();
        h2.setURL("jdbc:h2:mem:cmd-test;MODE=MySQL;DB_CLOSE_DELAY=-1");
        h2.setUser("sa");
        dataSource = h2;
        new org.springframework.jdbc.core.JdbcTemplate(dataSource).execute("DROP ALL OBJECTS");
        ResourceDatabasePopulator pop = new ResourceDatabasePopulator();
        pop.addScript(new ClassPathResource("schema.sql"));
        pop.execute(dataSource);

        taskRegistry = new TaskRegistry();
        ObjectProvider<ReconciliationCounter> counterProvider = mock(ObjectProvider.class);
        when(counterProvider.getIfAvailable()).thenReturn(null);
        commands = new MigrationCommands(
                taskRegistry, dataSource, new RegionClientRegistry(), new MigrationProperties(),
                MigrationNotifier.NO_OP, counterProvider,
                new TenantScanner.MySqlTenantScanner("tenant"));
    }

    /** 从 migrate 命令返回串解析 runId */
    private String extractRunId(String migrateResult) {
        // 格式: "Migration completed. runId=xxx, status=DONE"
        int start = migrateResult.indexOf("runId=") + 6;
        int end = migrateResult.indexOf(",", start);
        return migrateResult.substring(start, end);
    }

    @Test
    @DisplayName("migrate: 手动指定租户,完成迁移")
    void migrate_shouldComplete() {
        FakeMigrationTask task = new FakeMigrationTask("user-migration");
        taskRegistry.register(task);
        taskRegistry.registerCutover("user-migration", new RecordingCutoverAction());

        String result = commands.migrate("user-migration", "singapore", "myanmar",
                "p", "b", "t1,t2", 50, 1);

        assertThat(result).contains("Migration completed");
        assertThat(result).contains("status=DONE");
    }

    @Test
    @DisplayName("resume: 从断点续传 PENDING 租户")
    void resume_shouldContinuePending() {
        FakeMigrationTask task = new FakeMigrationTask("user-migration");
        taskRegistry.register(task);
        String runId = extractRunId(commands.migrate("user-migration", "singapore", "myanmar",
                "p", "b", "t1,t2", 50, 1));

        // 手动把 t1 回退为 PENDING 模拟中断
        new JdbcCheckpointStore(dataSource).updateTenantState(runId, "t1", TenantStatus.PENDING, null);
        task.getMigratedTenants().clear();

        commands.resume(runId, "user-migration");
        assertThat(task.getMigratedTenants()).contains("t1");
    }

    @Test
    @DisplayName("rollback: 对已完成租户回滚")
    void rollback_shouldReverseDone() {
        FakeMigrationTask task = new FakeMigrationTask("user-migration");
        taskRegistry.register(task);
        String runId = extractRunId(commands.migrate("user-migration", "singapore", "myanmar",
                "p", "b", "t1,t2", 50, 1));

        task.getMigratedTenants().clear();
        String result = commands.rollback(runId, "user-migration");

        assertThat(result).contains("Rollback completed");
        // 回滚对 t1/t2 再次 migrate
        assertThat(task.getMigratedTenants()).containsExactlyInAnyOrder("t1", "t2");
    }

    @Test
    @DisplayName("verify: 调用 task.verify 钩子,返回结果")
    void verify_shouldCallTaskVerify() {
        FakeMigrationTask task = new FakeMigrationTask("user-migration");
        taskRegistry.register(task);
        String runId = extractRunId(commands.migrate("user-migration", "singapore", "myanmar",
                "p", "b", "t1", 50, 1));

        String result = commands.verify(runId, "user-migration");

        assertThat(result).contains("Verify runId=");
        assertThat(result).contains("passed=true");
    }

    @Test
    @DisplayName("status: 查询迁移状态")
    void status_shouldReturnRunInfo() {
        FakeMigrationTask task = new FakeMigrationTask("user-migration");
        taskRegistry.register(task);
        String runId = extractRunId(commands.migrate("user-migration", "singapore", "myanmar",
                "p", "b", "t1", 50, 1));

        String result = commands.status(runId);

        assertThat(result).contains("runId=" + runId);
        assertThat(result).contains("status=DONE");
    }

    @Test
    @DisplayName("status: 不存在的 runId 返回提示")
    void status_unknownRun() {
        assertThat(commands.status("nonexistent")).contains("run not found");
    }

    @Test
    @DisplayName("status: 空 runId 返回 usage")
    void status_blankRunId() {
        assertThat(commands.status("")).contains("usage");
    }

    @Test
    @DisplayName("tasks: 列出已注册任务")
    void tasks_shouldListRegistered() {
        taskRegistry.register(new FakeMigrationTask("user-migration"));
        assertThat(commands.tasks()).contains("user-migration");
    }

    @Test
    @DisplayName("tasks: 无任务时返回提示")
    void tasks_empty() {
        assertThat(commands.tasks()).contains("no tasks");
    }

    @Test
    @DisplayName("dry-run: 手动指定租户返回数量")
    void dryRun_shouldReportTenantCount() {
        String result = commands.dryRun("singapore", "myanmar", "t1,t2,t3");
        assertThat(result).contains("tenantCount=3");
    }

    @Test
    @DisplayName("migrate: 未指定租户且无 scanner 配置时抛异常")
    void migrate_noTenantsNoScanner() {
        taskRegistry.register(new FakeMigrationTask("user-migration"));
        ObjectProvider<org.example.migration.engine.ReconciliationCounter> cp = mock(ObjectProvider.class);
        when(cp.getIfAvailable()).thenReturn(null);
        MigrationCommands cmd = new MigrationCommands(
                taskRegistry, dataSource, new RegionClientRegistry(), new MigrationProperties(),
                MigrationNotifier.NO_OP, cp, null);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        cmd.migrate("user-migration", "singapore", "myanmar", "p", "b", "", 50, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no tenants");
    }

    @Test
    @DisplayName("verify: 不存在的 runId 返回提示")
    void verify_unknownRun() {
        taskRegistry.register(new FakeMigrationTask("user-migration"));
        assertThat(commands.verify("nonexistent", "user-migration")).contains("run not found");
    }
}
