package org.example.migration.engine;

import org.example.migration.domain.entity.MigrationRun;

import java.util.List;

/**
 * 测试用 fake 对账闸门：可配置返回通过/不通过，验证"闸门控制切流"行为。
 */
public class FakeReconciliationGate implements ReconciliationGate {

    private final boolean pass;

    public FakeReconciliationGate(boolean pass) {
        this.pass = pass;
    }

    @Override
    public boolean check(MigrationRun run, List<String> migratedTenantIds) {
        return pass;
    }
}
