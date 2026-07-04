package org.example.migration.config;

import org.example.migration.client.AwsDynamoDbClient;
import org.example.migration.client.AwsS3Client;
import org.example.migration.client.ElasticEsClient;
import org.example.migration.client.JdbcMySqlClient;
import org.example.migration.client.RegionClientRegistry;
import org.example.migration.client.SpringKafkaClient;
import org.example.migration.client.SpringRedisClient;
import org.example.migration.domain.ClientType;
import org.example.migration.domain.RegionName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RegionClientAutoConfiguration 测试：验证配置驱动注册——给定 region 配置，
 * 各中间件客户端被正确创建并按三元组注册。
 *
 * 多实例：MySQL 按 dsName 注册（四参），Redis 按实例名注册（四参），
 * 单实例中间件（ES/S3/DynamoDB/Kafka）走三参（instance 占位 default）。
 */
@SpringBootTest(classes = {RegionClientAutoConfiguration.class}, webEnvironment = WebEnvironment.NONE)
@TestPropertySource(properties = {
        "regions.singapore.mysql.business.jdbc-url=jdbc:mysql://test/sg",
        "regions.singapore.mysql.business.username=u",
        "regions.singapore.mysql.business.password=p",
        "regions.singapore.redis.default.host=localhost",
        "regions.singapore.redis.default.port=6379",
        "regions.singapore.redis.default.password=pwd",
        "regions.singapore.elasticsearch.hosts=localhost:9200",
        "regions.singapore.elasticsearch.credentials=cred",
        "regions.singapore.s3.endpoint=http://localhost:9000",
        "regions.singapore.s3.bucket=sg-bucket",
        "regions.singapore.s3.access-key=ak",
        "regions.singapore.s3.secret-key=sk",
        "regions.singapore.dynamodb.endpoint=http://localhost:8000",
        "regions.singapore.dynamodb.region=ap-southeast-1",
        "regions.singapore.dynamodb.access-key=ak",
        "regions.singapore.dynamodb.secret-key=sk",
        "regions.singapore.kafka.brokers=localhost:9092",
        "regions.singapore.kafka.topic-prefix=singapore"
})
class RegionClientAutoConfigurationTest {

    @Autowired
    private RegionClientRegistry registry;

    @Test
    @DisplayName("MySQL 客户端按 instance 名注册（四参）")
    void shouldRegisterMysqlClientByInstance() {
        assertThat(registry.client(RegionName.SINGAPORE, ClientType.MYSQL, "business", JdbcMySqlClient.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Redis 客户端按 instance 名注册（四参）")
    void shouldRegisterRedisClientByInstance() {
        assertThat(registry.client(RegionName.SINGAPORE, ClientType.REDIS, "default", SpringRedisClient.class))
                .isNotNull();
    }

    @Test
    @DisplayName("ES 客户端按 region 注册（三参→default 占位）")
    void shouldRegisterEsClient() {
        assertThat(registry.client(RegionName.SINGAPORE, ClientType.ES, ElasticEsClient.class)).isNotNull();
    }

    @Test
    @DisplayName("S3 客户端按 region 注册（三参→default 占位）")
    void shouldRegisterS3Client() {
        assertThat(registry.client(RegionName.SINGAPORE, ClientType.S3, AwsS3Client.class)).isNotNull();
    }

    @Test
    @DisplayName("DynamoDB 客户端按 region 注册（三参→default 占位）")
    void shouldRegisterDynamoClient() {
        assertThat(registry.client(RegionName.SINGAPORE, ClientType.DYNAMODB, AwsDynamoDbClient.class)).isNotNull();
    }

    @Test
    @DisplayName("Kafka 客户端按 region 注册（三参→default 占位）")
    void shouldRegisterKafkaClient() {
        assertThat(registry.client(RegionName.SINGAPORE, ClientType.KAFKA, SpringKafkaClient.class)).isNotNull();
    }

    @Test
    @DisplayName("未配置的 region+type 四参查询抛 IllegalArgumentException")
    void shouldThrowForUnconfiguredRegion() {
        assertThatThrownBy(() ->
                registry.client(RegionName.MYANMAR, ClientType.MYSQL, "business", JdbcMySqlClient.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MYANMAR");
    }
}
