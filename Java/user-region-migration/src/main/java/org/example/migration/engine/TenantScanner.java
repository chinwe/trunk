package org.example.migration.engine;

import org.example.migration.client.MySqlClient;
import org.example.migration.domain.ClientType;
import org.example.migration.domain.RegionName;
import org.example.migration.spi.MigrationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 租户扫描器：从源区读取待迁移的租户ID列表。
 *
 * 抽象为接口便于测试注入 fake。生产实现从源区 MySQL 的 tenant 表读取。
 */
public interface TenantScanner {

    /** 扫描源区的全部租户ID */
    List<String> scanSourceTenants(MigrationContext ctx);

    /** MySQL 实现：查询 tenant 表 */
    class MySqlTenantScanner implements TenantScanner {

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
}
