package org.example.migration.engine;

import org.example.migration.domain.entity.MigrationRun;

import java.util.List;

/**
 * 默认对账闸门：总是通过。
 *
 * 骨架阶段占位实现。生产环境应替换为真实总量对账逻辑
 * （查询各中间件 COUNT 源 vs 目标）。
 * 真实实现可由业务通过自定义 ReconciliationGate Bean 覆盖。
 */
public class AlwaysPassReconciliationGate implements ReconciliationGate {

    @Override
    public boolean check(MigrationRun run, List<String> migratedTenantIds) {
        return true;
    }
}
