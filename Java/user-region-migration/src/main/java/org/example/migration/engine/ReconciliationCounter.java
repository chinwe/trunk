package org.example.migration.engine;

import org.example.migration.domain.RegionName;
import org.example.migration.domain.entity.MigrationRun;

/**
 * 对账计数器 SPI。业务实现，提供指定 region 在本次迁移范围内的记录总数。
 *
 * 框架的 CountReconciliationGate 调用它对比源/目标 region 的计数。
 * 计数方式由业务决定（如 MySQL COUNT、DynamoDB describeTable、S3 listObjects 计数等）。
 */
public interface ReconciliationCounter {

    /**
     * 统计指定 region 在本次迁移范围内的记录总数。
     *
     * @param region 区域
     * @param run    迁移执行上下文（含 task/product/bizLine，业务可据此决定统计范围）
     * @return 记录总数
     */
    long count(RegionName region, MigrationRun run);
}
