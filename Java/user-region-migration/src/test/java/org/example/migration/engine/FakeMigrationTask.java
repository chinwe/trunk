package org.example.migration.engine;

import org.example.migration.domain.MigrationPhase;
import org.example.migration.spi.MigrationContext;
import org.example.migration.spi.TenantMigrationTask;
import org.example.migration.spi.result.MigrationResult;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 测试用 fake 迁移任务：实现公共 SPI，记录收到的租户，可配置"哪些租户抛异常"。
 *
 * 这是测试替身而非 mock——它真实实现 TenantMigrationTask 接口，让引擎走真实代码路径。
 * 通过它的可观察状态（migratedTenants/failedTenants 集合）验证引擎行为。
 *
 * <p>两阶段（ADR-0005）：CORE 调用记入 {@link #coreMigratedTenants}，
 * SECONDARY 调用记入 {@link #secondaryMigratedTenants}。{@link #getMigratedTenants()}
 * 反映 CORE（向后兼容现有"迁移完成"断言），SECONDARY 用 {@link #getSecondaryMigratedTenants()}。
 *
 * <p>线程安全：批间并发下多个批会并发调用 migrate，内部记录集合用线程安全实现，避免并发写丢失元素。
 */
public class FakeMigrationTask implements TenantMigrationTask {

    private final String name;
    /** 配置为抛异常的租户ID集合（CORE 与 SECONDARY 阶段共享——任意阶段命中都抛） */
    private final Set<String> tenantsToFail = ConcurrentHashMap.newKeySet();

    /** 记录 CORE 阶段被成功迁移的租户（顺序保留）。线程安全 */
    private final List<String> coreMigratedTenants = new CopyOnWriteArrayList<>();
    /** 记录 SECONDARY 阶段被成功迁移的租户。线程安全 */
    private final List<String> secondaryMigratedTenants = new CopyOnWriteArrayList<>();
    /** 记录迁移失败的租户。线程安全 */
    private final List<String> failedTenants = new CopyOnWriteArrayList<>();

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
    public MigrationResult migrate(MigrationContext ctx, List<String> tenantIds, String product, String bizLine,
                                   MigrationPhase phase) {
        for (String tenantId : tenantIds) {
            if (tenantsToFail.contains(tenantId)) {
                failedTenants.add(tenantId);
                throw new RuntimeException("simulated failure for tenant " + tenantId);
            }
            if (phase == MigrationPhase.CORE) {
                coreMigratedTenants.add(tenantId);
            } else {
                secondaryMigratedTenants.add(tenantId);
            }
        }
        return MigrationResult.success(tenantIds.size());
    }

    /** CORE 阶段被成功迁移的租户 */
    public List<String> getMigratedTenants() {
        return coreMigratedTenants;
    }

    /** SECONDARY 阶段被成功迁移的租户 */
    public List<String> getSecondaryMigratedTenants() {
        return secondaryMigratedTenants;
    }

    public List<String> getFailedTenants() {
        return failedTenants;
    }

    /** 记录 CORE migrate 被调用的总次数（用于断点续传验证：是否重复处理） */
    public int getCallCount() {
        return coreMigratedTenants.size() + failedTenants.size();
    }
}
