package org.example.migration.engine;

import org.example.migration.client.RegionClient;
import org.example.migration.client.RegionClientRegistry;
import org.example.migration.config.MigrationProperties;
import org.example.migration.domain.ClientType;
import org.example.migration.domain.RegionName;
import org.example.migration.spi.MigrationContext;

/**
 * 基于 {@link RegionClientRegistry} 的 {@link MigrationContext} 实现。
 *
 * <p>统一的 Context 构造点（Q1/Q2）：消除引擎内部、命令层、scanner/verify 各自的 Context 重复。
 * 引擎与命令层都通过此实现提供 client 访问能力。
 */
public class RegistryMigrationContext implements MigrationContext {

    private final RegionName sourceRegion;
    private final RegionName targetRegion;
    private final RegionClientRegistry registry;
    private final MigrationProperties config;

    public RegistryMigrationContext(RegionName sourceRegion, RegionName targetRegion,
                                    RegionClientRegistry registry, MigrationProperties config) {
        this.sourceRegion = sourceRegion;
        this.targetRegion = targetRegion;
        this.registry = registry;
        this.config = config;
    }

    @Override
    public RegionName sourceRegion() {
        return sourceRegion;
    }

    @Override
    public RegionName targetRegion() {
        return targetRegion;
    }

    @Override
    public <C extends RegionClient> C client(RegionName region, ClientType type, Class<C> clazz) {
        return registry.client(region, type, clazz);
    }

    @Override
    public <C extends RegionClient> C client(RegionName region, ClientType type, String instance, Class<C> clazz) {
        return registry.client(region, type, instance, clazz);
    }

    @Override
    public MigrationProperties config() {
        return config;
    }
}
