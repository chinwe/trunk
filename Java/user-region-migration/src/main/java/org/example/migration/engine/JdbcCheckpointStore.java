package org.example.migration.engine;

import org.example.migration.domain.RunStatus;
import org.example.migration.domain.TenantStatus;
import org.example.migration.domain.entity.MigrationRun;
import org.example.migration.domain.entity.MigrationTenantState;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JDBC 版 CheckpointStore。基于 MySQL（测试用 H2）。
 * 行为与 InMemoryCheckpointStore 一致，共享同一套契约测试。
 *
 * <p>createRun 用单事务包裹（R4）：避免插到一半 DB 异常时留下"有 run 无部分 tenant_state"的脏数据。
 */
public class JdbcCheckpointStore implements CheckpointStore {

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;

    public JdbcCheckpointStore(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.tx = new TransactionTemplate(
                new org.springframework.jdbc.datasource.DataSourceTransactionManager(dataSource));
    }

    @Override
    public void createRun(MigrationRun run, List<String> tenantIds) {
        LocalDateTime now = LocalDateTime.now();
        run.setStartedAt(now);
        run.setUpdatedAt(now);
        // 单事务：run 行 + 全部 tenant_state 行要么全成功要么全回滚
        tx.executeWithoutResult(status -> {
            jdbc.update("""
                    INSERT INTO migration_run
                      (run_id, task_name, direction, source_region, target_region,
                       product, biz_line, status, phase, total_tenants, processed_tenants, failed_tenants,
                       started_at, updated_at, error_context, parent_run_id)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                    """,
                    run.getRunId(), run.getTaskName(), run.getDirection().name(),
                    run.getSourceRegion().value(), run.getTargetRegion().value(),
                    run.getProduct(), run.getBizLine(), run.getStatus().name(),
                    run.getPhase() != null ? run.getPhase().name() : null,
                    run.getTotalTenants(), 0, 0,
                    Timestamp.valueOf(now), Timestamp.valueOf(now),
                    run.getErrorContext(), run.getParentRunId());

            for (String tenantId : tenantIds) {
                jdbc.update("""
                        INSERT INTO migration_tenant_state
                          (run_id, tenant_id, status, error_context, updated_at)
                        VALUES (?,?,?,?,?)
                        """,
                        run.getRunId(), tenantId, TenantStatus.PENDING.name(),
                        null, Timestamp.valueOf(now));
            }
        });
    }

    @Override
    public MigrationRun findRun(String runId) {
        List<MigrationRun> runs = jdbc.query("""
                SELECT run_id, task_name, direction, source_region, target_region,
                       product, biz_line, status, phase, total_tenants, processed_tenants, failed_tenants,
                       started_at, updated_at, error_context, parent_run_id
                FROM migration_run WHERE run_id = ?
                """,
                (rs, rowNum) -> mapRun(rs),
                runId);
        return runs.isEmpty() ? null : runs.get(0);
    }

    @Override
    public void updateTenantState(String runId, String tenantId, TenantStatus status, String errorContext) {
        jdbc.update("""
                UPDATE migration_tenant_state
                SET status = ?, error_context = ?, updated_at = ?
                WHERE run_id = ? AND tenant_id = ?
                """,
                status.name(), errorContext, Timestamp.valueOf(LocalDateTime.now()),
                runId, tenantId);
    }

    @Override
    public List<String> findTenantIdsByStatus(String runId, TenantStatus status) {
        return jdbc.queryForList("""
                SELECT tenant_id FROM migration_tenant_state
                WHERE run_id = ? AND status = ?
                """, String.class, runId, status.name());
    }

    @Override
    public List<MigrationTenantState> findTenantStatesByStatus(String runId, TenantStatus status) {
        return jdbc.query("""
                SELECT run_id, tenant_id, status, error_context, updated_at
                FROM migration_tenant_state
                WHERE run_id = ? AND status = ?
                """,
                (rs, rowNum) -> mapTenantState(rs),
                runId, status.name());
    }

    @Override
    public void updateRunProgress(String runId, int processedTenants, int failedTenants, RunStatus status) {
        jdbc.update("""
                UPDATE migration_run
                SET processed_tenants = ?, failed_tenants = ?, status = ?, updated_at = ?
                WHERE run_id = ?
                """,
                processedTenants, failedTenants, status.name(),
                Timestamp.valueOf(LocalDateTime.now()), runId);
    }

    private MigrationRun mapRun(java.sql.ResultSet rs) throws java.sql.SQLException {
        MigrationRun run = new MigrationRun();
        run.setRunId(rs.getString("run_id"));
        run.setTaskName(rs.getString("task_name"));
        run.setDirection(org.example.migration.domain.Direction.valueOf(rs.getString("direction")));
        run.setSourceRegion(org.example.migration.domain.RegionName.of(rs.getString("source_region")));
        run.setTargetRegion(org.example.migration.domain.RegionName.of(rs.getString("target_region")));
        run.setProduct(rs.getString("product"));
        run.setBizLine(rs.getString("biz_line"));
        run.setStatus(RunStatus.valueOf(rs.getString("status")));
        String phaseStr = rs.getString("phase");
        run.setPhase(phaseStr != null
                ? org.example.migration.domain.MigrationPhase.valueOf(phaseStr) : null);
        run.setTotalTenants(rs.getInt("total_tenants"));
        run.setProcessedTenants(rs.getInt("processed_tenants"));
        run.setFailedTenants(rs.getInt("failed_tenants"));
        Timestamp startedAt = rs.getTimestamp("started_at");
        run.setStartedAt(startedAt != null ? startedAt.toLocalDateTime() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        run.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
        run.setErrorContext(rs.getString("error_context"));
        run.setParentRunId(rs.getString("parent_run_id"));
        return run;
    }

    private MigrationTenantState mapTenantState(java.sql.ResultSet rs) throws java.sql.SQLException {
        MigrationTenantState state = new MigrationTenantState();
        state.setRunId(rs.getString("run_id"));
        state.setTenantId(rs.getString("tenant_id"));
        state.setStatus(TenantStatus.valueOf(rs.getString("status")));
        state.setErrorContext(rs.getString("error_context"));
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        state.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
        return state;
    }
}
