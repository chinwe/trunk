package org.example.migration.engine;

import org.example.migration.domain.RunStatus;
import org.example.migration.domain.TenantStatus;
import org.example.migration.domain.entity.MigrationRun;
import org.example.migration.domain.entity.MigrationTenantState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存版 CheckpointStore。测试与本地开发用，不持久化。
 *
 * 行为与 JdbcCheckpointStore 一致，可作为 store 契约的参考实现。
 */
public class InMemoryCheckpointStore implements CheckpointStore {

    private final Map<String, MigrationRun> runs = new ConcurrentHashMap<>();
    /** key = runId, value = 该 run 的租户状态列表 */
    private final Map<String, List<MigrationTenantState>> tenantStates = new ConcurrentHashMap<>();

    @Override
    public void createRun(MigrationRun run, List<String> tenantIds) {
        run.setStartedAt(LocalDateTime.now());
        run.setUpdatedAt(LocalDateTime.now());
        runs.put(run.getRunId(), run);

        List<MigrationTenantState> states = new ArrayList<>();
        for (String tenantId : tenantIds) {
            MigrationTenantState state = new MigrationTenantState();
            state.setRunId(run.getRunId());
            state.setTenantId(tenantId);
            state.setStatus(TenantStatus.PENDING);
            state.setUpdatedAt(LocalDateTime.now());
            states.add(state);
        }
        tenantStates.put(run.getRunId(), states);
    }

    @Override
    public MigrationRun findRun(String runId) {
        return runs.get(runId);
    }

    @Override
    public void updateTenantState(String runId, String tenantId, TenantStatus status, String errorContext) {
        List<MigrationTenantState> states = tenantStates.get(runId);
        if (states == null) {
            return;
        }
        for (MigrationTenantState state : states) {
            if (state.getTenantId().equals(tenantId)) {
                state.setStatus(status);
                state.setErrorContext(errorContext);
                state.setUpdatedAt(LocalDateTime.now());
                return;
            }
        }
    }

    @Override
    public List<String> findTenantIdsByStatus(String runId, TenantStatus status) {
        List<MigrationTenantState> states = tenantStates.get(runId);
        if (states == null) {
            return List.of();
        }
        return states.stream()
                .filter(s -> s.getStatus() == status)
                .map(MigrationTenantState::getTenantId)
                .toList();
    }

    @Override
    public List<MigrationTenantState> findTenantStatesByStatus(String runId, TenantStatus status) {
        List<MigrationTenantState> states = tenantStates.get(runId);
        if (states == null) {
            return List.of();
        }
        return states.stream()
                .filter(s -> s.getStatus() == status)
                .toList();
    }

    @Override
    public void updateRunProgress(String runId, int processedTenants, int failedTenants, RunStatus status) {
        MigrationRun run = runs.get(runId);
        if (run == null) {
            return;
        }
        run.setProcessedTenants(processedTenants);
        run.setFailedTenants(failedTenants);
        run.setStatus(status);
        run.setUpdatedAt(LocalDateTime.now());
    }
}
