package org.example.migration.client;

import java.util.List;

/**
 * Redis 客户端。会话/缓存类数据迁移。
 */
public non-sealed interface RedisClient extends RegionClient {

    /**
     * 按租户ID列表读取关联的 key 集合。
     * key 的组织方式由业务决定（如 session:{tenantId}:*），此处仅给 pattern 约定。
     */
    List<String> scanKeysByTenants(String keyPattern, List<String> tenantIds);

    /** 按 key 读取 value */
    String get(String key);

    /** 写入 key-value */
    void set(String key, String value);

    /** 按 key 删除（回滚/清理用） */
    void delete(String key);
}
