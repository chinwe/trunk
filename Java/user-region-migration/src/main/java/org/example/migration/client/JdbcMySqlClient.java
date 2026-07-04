package org.example.migration.client;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * MySQL 客户端的 JDBC 适配器实现。
 * 基于 JdbcTemplate，sql 中用 :tenants 占位（业务侧约定），本适配器负责展开 IN 列表。
 *
 * 简化约定：sql 中的 ":tenants" 会被替换为 "?,?,..." 并按位置绑定 tenantIds。
 */
public class JdbcMySqlClient implements MySqlClient {

    private final JdbcTemplate jdbc;

    public JdbcMySqlClient(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    /** 供测试与装配检查使用 */
    public JdbcMySqlClient(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<?> queryByTenants(String sql, List<String> tenantIds) {
        String prepared = expandInPlaceholder(sql, tenantIds.size());
        return jdbc.queryForList(prepared, tenantIds.toArray());
    }

    @Override
    public List<Map<String, Object>> queryForList(String sql, Object... args) {
        return jdbc.queryForList(sql, args);
    }

    @Override
    public int[] batchUpdate(String sql, List<Object[]> argsList) {
        return jdbc.batchUpdate(sql, argsList);
    }

    @Override
    public int deleteByTenants(String sql, List<String> tenantIds) {
        String prepared = expandInPlaceholder(sql, tenantIds.size());
        return jdbc.update(prepared, tenantIds.toArray());
    }

    @Override
    public Object raw() {
        return jdbc;
    }

    /** 把 sql 中的 ":tenants" 替换为与租户数匹配的 IN 占位符 */
    static String expandInPlaceholder(String sql, int tenantCount) {
        if (tenantCount <= 0) {
            return sql.replace(":tenants", "NULL");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tenantCount; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('?');
        }
        return sql.replace(":tenants", sb.toString());
    }
}
