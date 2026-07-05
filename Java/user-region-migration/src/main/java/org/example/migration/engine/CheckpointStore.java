package org.example.migration.engine;

import org.example.migration.domain.TenantStatus;
import org.example.migration.domain.entity.MigrationRun;
import org.example.migration.domain.entity.MigrationTenantState;

import java.util.List;

/**
 * 迁移状态存储抽象。引擎依赖此接口，与具体存储介质解耦。
 *
 * 实现方：
 * - InMemoryCheckpointStore：测试用，纯内存
 * - JdbcCheckpointStore：生产用，基于 MySQL
 *
 * 语义保证（所有实现必须遵守）：
 * - 创建 run 时所有租户初始化为 PENDING
 * - 租户状态翻转通过 updateTenantState 显式提交
 * - 查询按 status 精确返回
 */
public interface CheckpointStore {

    /** 创建一次迁移执行，并将所有租户初始化为 PENDING */
    void createRun(MigrationRun run, List<String> tenantIds);

    /** 按 runId 读取迁移执行。不存在返回 null。 */
    MigrationRun findRun(String runId);

    /** 翻转租户状态（DONE/FAILED/RUNNING），并记录 errorContext（FAILED 时用） */
    void updateTenantState(String runId, String tenantId, TenantStatus status, String errorContext);

    /** 查询某 run 下指定状态的租户ID列表（DONE 用于回滚，FAILED 用于汇总） */
    List<String> findTenantIdsByStatus(String runId, TenantStatus status);

    /** 查询某 run 下指定状态的完整租户状态记录（含 errorContext，FAILED 汇总用） */
    List<MigrationTenantState> findTenantStatesByStatus(String runId, TenantStatus status);

    /** 更新 run 的统计计数与状态（processed/failed/status） */
    void updateRunProgress(String runId, int processedTenants, int failedTenants,
                           org.example.migration.domain.RunStatus status);

    /**
     * 将该 run 下所有 DONE 的租户批量重置为 PENDING，并返回该 run 的全部租户 ID
     * （不分状态）。用于阶段切换时一次性重建租户列表。
     *
     * <p>实现必须保证：
     * <ul>
     *   <li>重置与收集在逻辑上原子：收集到的列表必须反映重置后的状态</li>
     *   <li>FAILED 租户包含在返回列表中（调用方自行决定是否跳过）</li>
     * </ul>
     *
     * @param runId 迁移执行标识
     * @return 全部租户 ID 列表，顺序不作保证
     */
    List<String> resetDoneAndListTenants(String runId);
}
