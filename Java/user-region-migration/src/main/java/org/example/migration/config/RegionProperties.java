package org.example.migration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 区域连接配置：以 region 名为一等公民组织各中间件连接信息。
 * 对应 application.yml 的 regions.* 结构。
 *
 * 不指定 prefix：字段名 regions 直接对应 yaml 顶层 key "regions"，
 * region 名作为 Map 的动态 key。
 */
@ConfigurationProperties
public class RegionProperties {

    /** region 名 -> 该 region 的全部中间件配置。key 如 "singapore" / "myanmar" */
    private Map<String, RegionConfig> regions;

    public Map<String, RegionConfig> getRegions() {
        return regions;
    }

    public void setRegions(Map<String, RegionConfig> regions) {
        this.regions = regions;
    }

    /** 单个 region 的所有中间件配置 */
    public static class RegionConfig {
        /** 数据源名 -> 数据源配置。key 如 "business"（业务库）/ "tool-state"（工具状态库） */
        private Map<String, DataSourceConfig> mysql;
        private RedisConfig redis;
        private ElasticsearchConfig elasticsearch;
        private S3Config s3;
        private DynamoDbConfig dynamodb;
        private KafkaConfig kafka;

        public Map<String, DataSourceConfig> getMysql() { return mysql; }
        public void setMysql(Map<String, DataSourceConfig> mysql) { this.mysql = mysql; }
        public RedisConfig getRedis() { return redis; }
        public void setRedis(RedisConfig redis) { this.redis = redis; }
        public ElasticsearchConfig getElasticsearch() { return elasticsearch; }
        public void setElasticsearch(ElasticsearchConfig elasticsearch) { this.elasticsearch = elasticsearch; }
        public S3Config getS3() { return s3; }
        public void setS3(S3Config s3) { this.s3 = s3; }
        public DynamoDbConfig getDynamodb() { return dynamodb; }
        public void setDynamodb(DynamoDbConfig dynamodb) { this.dynamodb = dynamodb; }
        public KafkaConfig getKafka() { return kafka; }
        public void setKafka(KafkaConfig kafka) { this.kafka = kafka; }
    }

    /** MySQL/JDBC 数据源配置 */
    public static class DataSourceConfig {
        private String jdbcUrl;
        private String username;
        private String password;

        public String getJdbcUrl() { return jdbcUrl; }
        public void setJdbcUrl(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /** Redis 配置 */
    public static class RedisConfig {
        private String host;
        private int port;
        private String password;

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /** Elasticsearch 配置 */
    public static class ElasticsearchConfig {
        private String hosts;
        private String credentials;

        public String getHosts() { return hosts; }
        public void setHosts(String hosts) { this.hosts = hosts; }
        public String getCredentials() { return credentials; }
        public void setCredentials(String credentials) { this.credentials = credentials; }
    }

    /** S3 对象存储配置 */
    public static class S3Config {
        private String endpoint;
        private String bucket;
        private String accessKey;
        private String secretKey;

        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }
        public String getAccessKey() { return accessKey; }
        public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    }

    /** DynamoDB 配置 */
    public static class DynamoDbConfig {
        private String endpoint;
        private String region;
        private String accessKey;
        private String secretKey;

        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getAccessKey() { return accessKey; }
        public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    }

    /** Kafka 配置 */
    public static class KafkaConfig {
        private String brokers;
        private String topicPrefix;

        public String getBrokers() { return brokers; }
        public void setBrokers(String brokers) { this.brokers = brokers; }
        public String getTopicPrefix() { return topicPrefix; }
        public void setTopicPrefix(String topicPrefix) { this.topicPrefix = topicPrefix; }
    }
}
