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
        @SuppressWarnings("unchecked")
        public List<String> scanSourceTenants(MigrationContext ctx) {
            MySqlClient mysql = ctx.client(ctx.sourceRegion(), ClientType.MYSQL, MySqlClient.class);
            // 复用 queryByTenants 不合适（它按租户过滤），这里直接用 raw JdbcTemplate 查全表
            org.springframework.jdbc.core.JdbcTemplate jdbc =
                    (org.springframework.jdbc.core.JdbcTemplate) mysql.raw();
            List<Map<String, Object>> rows = jdbc.queryForList(
                    "SELECT id FROM " + tableName);
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
