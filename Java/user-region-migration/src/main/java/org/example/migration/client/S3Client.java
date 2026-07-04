package org.example.migration.client;

import java.util.List;

/**
 * S3 对象存储客户端。大文件/对象迁移。
 */
public interface S3Client extends RegionClient {

    /** 返回当前客户端所操作的 bucket 名称 */
    String getBucket();

    /** 列出某租户关联的对象 key（{tenant} 占位用 tenantId 替换） */
    List<String> listKeysByTenants(String prefix, List<String> tenantIds);

    /** 跨区拷贝对象（从当前 bucket 拷到目标客户端的 bucket） */
    void copyObject(String sourceKey, S3Client targetClient, String targetKey);

    /** 判断对象是否存在 */
    boolean exists(String key);

    /** 上传对象内容 */
    void putObject(String key, byte[] content);

    /** 删除对象（回滚/清理用） */
    void delete(String key);
}
