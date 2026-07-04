package org.example.migration.client;

/**
 * 区域中间件客户端抽象。所有具体中间件客户端（MySqlClient/RedisClient/EsClient/
 * S3Client/DynamoDbClient/KafkaClient）实现此接口。
 *
 * 业务插件通过 MigrationContext.client(region, type) 获取具体类型客户端。
 * 各子接口只暴露跨区迁移高频用到的最小操作集；复杂查询可通过 raw() 拿原生客户端。
 */
public interface RegionClient {

    /**
     * 逃生口：返回原生客户端（如 JdbcTemplate / AWS S3Client / RedisTemplate）。
     * 用于框架未封装的复杂操作。返回类型为 Object，由调用方自行转换。
     */
    Object raw();
}
