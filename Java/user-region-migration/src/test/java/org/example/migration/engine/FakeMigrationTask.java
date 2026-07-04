package org.example.migration.engine;

import org.example.migration.spi.MigrationContext;
import org.example.migration.spi.TenantMigrationTask;
import org.example.migration.spi.result.MigrationResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 测试用 fake 迁移任务：实现公共 SPI，记录收到的租户，可配置"哪些租户抛异常"。
 *
 * 这是测试替身而非 mock——它真实实现 TenantMigrationTask 接口，让引擎走真实代码路径。
 * 通过它的可观察状态（migratedTenants/failedTenants 集合）验证引擎行为。
 */
public class FakeMigrationTask implements TenantMigrationTask {

    private final String name;
    /** 配置为抛异常的租户ID集合 */
    private final Set<String> tenantsToFail = new HashSet<>();

    /** 记录被成功迁移的租户（顺序保留，验证调用用） */
    private final List<String> migratedTenants = new ArrayList<>();
    /** 记录迁移失败的租户 */
    private final List<String> failedTenants = new ArrayList<>();

    public FakeMigrationTask(String name) {
        this.name = name;
    }

    /** 配置某个租户迁移时抛异常（模拟业务失败） */
    public FakeMigrationTask failOn(String tenantId) {
        tenantsToFail.add(tenantId);
        return this;
    }

    @Override
    public String taskName() {
        return name;
    }

    @Override
    public MigrationResult migrate(MigrationContext ctx, List<String> tenantIds, String product, String bizLine) {
        for (String tenantId : tenantIds) {
            if (tenantsToFail.contains(tenantId)) {
                failedTenants.add(tenantId);
                throw new RuntimeException("simulated failure for tenant " + tenantId);
            }
            migratedTenants.add(tenantId);
        }
        return MigrationResult.success(tenantIds.size());
    }

    public List<String> getMigratedTenants() {
        return migratedTenants;
    }

    public List<String> getFailedTenants() {
        return failedTenants;
    }

    /** 记录 migrate 被调用的总次数（用于断点续传验证：是否重复处理） */
    public int getCallCount() {
        return migratedTenants.size() + failedTenants.size();
    }
}
