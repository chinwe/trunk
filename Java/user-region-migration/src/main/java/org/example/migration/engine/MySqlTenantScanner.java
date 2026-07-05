package org.example.migration.engine;

import org.example.migration.client.MySqlClient;
import org.example.migration.domain.ClientType;
import org.example.migration.spi.MigrationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link TenantScanner} 的 MySQL 适配器：从源区 tenant 表读取租户 ID。
 *
 * <p>生产默认实现。查询 {@code SELECT id FROM <tableName>}，
 * 通过 {@link MySqlClient#queryForList} 避免裸调 {@code raw()} 强转 JdbcTemplate（D4）。
 *
 * <p>业务如把 tenant 表放在命名实例，可自定义 TenantScanner 实现替代本类。
 */
public class MySqlTenantScanner implements TenantScanner {

    private final String tableName;

    public MySqlTenantScanner(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public List<String> scanSourceTenants(MigrationContext ctx) {
        // 注：这里用三参 client API（instance=default），tenant 表通常在 default 实例；
        // 业务如把 tenant 表放在命名实例，可自定义 TenantScanner 实现。
        MySqlClient mysql = ctx.client(ctx.sourceRegion(), ClientType.MYSQL, MySqlClient.class);
        // 用 MySqlClient.queryForList 而非 raw() 强转（D4，避免抽象泄漏）
        List<Map<String, Object>> rows = mysql.queryForList("SELECT id FROM " + tableName);
        List<String> tenantIds = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Object id = row.get("id");
            if (id != null) {
                tenantIds.add(String.valueOf(id));
            }
        }
        return tenantIds;
    }
}
