package org.example.migration.client;

import java.util.List;

/**
 * Elasticsearch 客户端。索引数据迁移。
 */
public interface EsClient extends RegionClient {

    /** 按租户ID列表查询索引文档 */
    List<?> searchByTenants(String index, List<String> tenantIds);

    /** 批量索引文档 */
    void bulkIndex(String index, List<?> documents);

    /** 按租户ID列表删除文档（回滚/清理用） */
    void deleteByTenants(String index, List<String> tenantIds);
}
