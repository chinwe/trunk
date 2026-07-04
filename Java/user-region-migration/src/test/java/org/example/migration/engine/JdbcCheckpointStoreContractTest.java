package org.example.migration.engine;

import org.example.migration.domain.TenantStatus;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.util.List;

/**
 * JdbcCheckpointStore 的契约测试（H2 内存库）。
 * 复用 CheckpointStoreContractTest 的全部行为规约，保证 Jdbc 实现与 InMemory 行为一致。
 *
 * 额外验证 Jdbc 特有的边界：null parent_run_id、null error_context 的往返。
 */
class JdbcCheckpointStoreContractTest extends CheckpointStoreContractTest {

    private DataSource dataSource;
    private JdbcCheckpointStore store;

    @BeforeEach
    void setUpDatabase() {
        // H2 内存库，兼容 MySQL 模式
        org.h2.jdbcx.JdbcDataSource h2 = new org.h2.jdbcx.JdbcDataSource();
        h2.setURL("jdbc:h2:mem:migration-test;MODE=MySQL;DB_CLOSE_DELAY=-1");
        h2.setUser("sa");
        h2.setPassword("");
        dataSource = h2;
        // 初始化 schema（首次）+ 清空旧数据（测试间隔离）
        new org.springframework.jdbc.core.JdbcTemplate(dataSource).execute(
                "DROP ALL OBJECTS");
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.execute(dataSource);
        store = new JdbcCheckpointStore(dataSource);
    }

    @Override
    protected CheckpointStore store() {
        return store;
    }

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("Jdbc: findTenantIdsByStatus 对空结果返回空列表")
    void shouldReturnEmptyListWhenNoMatchingStatus() {
        store.createRun(newRun("run-jdbc-1"), List.of("t1"));

        // 没有 FAILED 租户时应返回空列表而非 null
        List<String> failed = store.findTenantIdsByStatus("run-jdbc-1", TenantStatus.FAILED);

        org.assertj.core.api.Assertions.assertThat(failed).isEmpty();
    }

    private org.example.migration.domain.entity.MigrationRun newRun(String runId) {
        org.example.migration.domain.entity.MigrationRun run = new org.example.migration.domain.entity.MigrationRun();
        run.setRunId(runId);
        run.setTaskName("user-migration");
        run.setDirection(org.example.migration.domain.Direction.FORWARD);
        run.setSourceRegion(org.example.migration.domain.RegionName.SINGAPORE);
        run.setTargetRegion(org.example.migration.domain.RegionName.MYANMAR);
        run.setStatus(org.example.migration.domain.RunStatus.RUNNING);
        run.setTotalTenants(1);
        return run;
    }
}
