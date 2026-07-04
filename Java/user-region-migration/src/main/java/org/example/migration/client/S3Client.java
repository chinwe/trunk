package org.example.migration.client;

import java.util.List;

/**
 * S3 对象存储客户端。大文件/对象迁移。
 */
public non-sealed interface S3Client extends RegionClient {

    /** 列出某租户关联的对象 key */
    List<String> listKeysByTenants(String prefix, List<String> tenantIds);

    /** 跨区拷贝对象（从一个 region 的客户端拷到另一个 region 的客户端） */
    void copyObject(String sourceKey, S3Client targetClient, String targetKey);

    /** 判断对象是否存在 */
    boolean exists(String key);

    /** 删除对象（回滚/清理用） */
    void delete(String key);
}
