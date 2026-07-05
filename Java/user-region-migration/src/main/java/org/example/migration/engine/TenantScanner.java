package org.example.migration.engine;

import org.example.migration.spi.MigrationContext;

import java.util.List;

/**
 * 租户扫描器：从源区读取待迁移的租户ID列表。
 *
 * 抽象为接口便于测试注入 fake。生产实现见 {@link MySqlTenantScanner}。
 */
public interface TenantScanner {

    /** 扫描源区的全部租户ID */
    List<String> scanSourceTenants(MigrationContext ctx);
}
