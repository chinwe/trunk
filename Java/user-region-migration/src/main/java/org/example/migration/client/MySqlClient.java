package org.example.migration.client;

import java.util.List;
import java.util.Map;

/**
 * MySQL 客户端。封装跨区迁移高频操作。
 * 详细操作随业务插件需要补充。
 */
public interface MySqlClient extends RegionClient {

    /**
     * 按租户ID列表查询业务记录。
     *
     * @param sql       查询语句，应包含租户ID的 IN 占位
     * @param tenantIds 租户ID列表
     * @return 业务记录列表（由调用方解释）
     */
    List<?> queryByTenants(String sql, List<String> tenantIds);

    /**
     * 通用查询：执行任意返回多行的 SQL（不按租户过滤，如扫描 tenant 表全表）。
     * 框架内部 TenantScanner 用此方法，避免裸调 raw() 强转 JdbcTemplate（D4）。
     *
     * @param sql 查询语句
     * @param args 绑定参数
     * @return 行列表（每行是列名→值的 Map）
     */
    List<Map<String, Object>> queryForList(String sql, Object... args);

    /**
     * 批量写入。
     *
     * @param sql     写入语句
     * @param argsList 每行参数
     * @return 每行影响行数
     */
    int[] batchUpdate(String sql, List<Object[]> argsList);

    /**
     * 按租户ID列表删除（回滚/清理用）。
     */
    int deleteByTenants(String sql, List<String> tenantIds);
}
