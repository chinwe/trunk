package org.example.migration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 区域连接配置：以 region 名为一等公民组织各中间件连接信息。
 * 对应 application.yml 的 regions.* 结构。
 *
 * 不指定 prefix：字段名 regions 直接对应 yaml 顶层 key "regions"，
 * region 名作为 Map 的动态 key。
 */
@Data
@ConfigurationProperties
public class RegionProperties {

    /** region 名 -> 该 region 的全部中间件配置。key 如 "singapore" / "myanmar" */
    private Map<String, RegionConfig> regions;

    /** 单个 region 的所有中间件配置 */
    @Data
    public static class RegionConfig {
        /** MySQL 实例名 -> 数据源配置。key 如 "business"（业务库）/ "open"（开放平台库） */
        private Map<String, DataSourceConfig> mysql;
        /** Redis 实例名 -> 配置。key 如 "session"（会话）/ "cache"（缓存） */
        private Map<String, RedisConfig> redis;
        private ElasticsearchConfig elasticsearch;
        private S3Config s3;
        private DynamoDbConfig dynamodb;
        private KafkaConfig kafka;
    }

    /** MySQL/JDBC 数据源配置 */
    @Data
    public static class DataSourceConfig {
        private String jdbcUrl;
        private String username;
        private String password;
    }

    /** Redis 配置 */
    @Data
    public static class RedisConfig {
        private String host;
        private int port;
        private String password;
    }

    /** Elasticsearch 配置 */
    @Data
    public static class ElasticsearchConfig {
        private String hosts;
        private String credentials;
    }

    /** S3 对象存储配置 */
    @Data
    public static class S3Config {
        private String endpoint;
        private String bucket;
        private String accessKey;
        private String secretKey;
    }

    /** DynamoDB 配置 */
    @Data
    public static class DynamoDbConfig {
        private String endpoint;
        private String region;
        private String accessKey;
        private String secretKey;
    }

    /** Kafka 配置 */
    @Data
    public static class KafkaConfig {
        private String brokers;
        private String topicPrefix;
    }
}
