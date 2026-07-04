package org.example.migration.client;

import java.util.List;

/**
 * DynamoDB 客户端。文档型数据迁移。
 */
public non-sealed interface DynamoDbClient extends RegionClient {

    /** 按租户ID列表查询 Item */
    List<?> queryByTenants(String tableName, List<String> tenantIds);

    /** 批量写入 Item */
    void batchPutItems(String tableName, List<?> items);

    /** 按租户ID列表删除 Item（回滚/清理用） */
    void deleteByTenants(String tableName, List<String> tenantIds);
}
