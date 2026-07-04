package org.example.migration.config;

import org.example.migration.client.AwsDynamoDbClient;
import org.example.migration.client.AwsS3Client;
import org.example.migration.client.DynamoDbClient;
import org.example.migration.client.ElasticEsClient;
import org.example.migration.client.JdbcMySqlClient;
import org.example.migration.client.KafkaClient;
import org.example.migration.client.MySqlClient;
import org.example.migration.client.RegionClientRegistry;
import org.example.migration.client.S3Client;
import org.example.migration.client.SpringKafkaClient;
import org.example.migration.client.SpringRedisClient;
import org.example.migration.client.EsClient;
import org.example.migration.client.RedisClient;
import org.example.migration.domain.ClientType;
import org.example.migration.domain.RegionName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import javax.sql.DataSource;
import java.net.URI;
import java.util.Map;

/**
 * 客户端自动装配：根据 RegionProperties 为每个 region 的每个中间件创建客户端并注册。
 * 加新 region 只改 yml，零 Java 改动。
 *
 * 客户端创建遵循"按需创建"原则：某 region 某中间件未配置则跳过（不报错）。
 */
@Configuration
@EnableConfigurationProperties({ RegionProperties.class, MigrationProperties.class })
public class RegionClientAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RegionClientAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public RegionClientRegistry regionClientRegistry(RegionProperties props) {
        RegionClientRegistry registry = new RegionClientRegistry();
        if (props.getRegions() == null) {
            return registry;
        }
        props.getRegions().forEach((regionName, cfg) -> {
            if (cfg == null) {
                return;
            }
            RegionName region = RegionName.of(regionName);
            registerMysql(registry, region, cfg);
            registerRedis(registry, region, cfg);
            registerEs(registry, region, cfg);
            registerS3(registry, region, cfg);
            registerDynamo(registry, region, cfg);
            registerKafka(registry, region, cfg);
        });
        return registry;
    }

    private void registerMysql(RegionClientRegistry registry, RegionName region, RegionProperties.RegionConfig cfg) {
        if (cfg.getMysql() == null) {
            return;
        }
        cfg.getMysql().forEach((dsName, dsCfg) -> {
            DataSource dataSource = new DriverManagerDataSource(
                    dsCfg.getJdbcUrl(), dsCfg.getUsername(), dsCfg.getPassword());
            registry.register(region, ClientType.MYSQL, dsName, new JdbcMySqlClient(dataSource));
            log.info("registered MySQL client [{}] for region {}", dsName, region);
        });
    }

    private void registerRedis(RegionClientRegistry registry, RegionName region, RegionProperties.RegionConfig cfg) {
        if (cfg.getRedis() == null) {
            return;
        }
        cfg.getRedis().forEach((instanceName, rc) -> {
            RedisStandaloneConfiguration redisCfg = new RedisStandaloneConfiguration(rc.getHost(), rc.getPort());
            if (rc.getPassword() != null) {
                redisCfg.setPassword(rc.getPassword());
            }
            LettuceConnectionFactory factory = new LettuceConnectionFactory(redisCfg);
            factory.afterPropertiesSet();
            RedisTemplate<String, String> template = new RedisTemplate<>();
            template.setConnectionFactory(factory);
            template.afterPropertiesSet();
            registry.register(region, ClientType.REDIS, instanceName, new SpringRedisClient(template));
            log.info("registered Redis client [{}] for region {}", instanceName, region);
        });
    }

    private void registerEs(RegionClientRegistry registry, RegionName region, RegionProperties.RegionConfig cfg) {
        if (cfg.getElasticsearch() == null) {
            return;
        }
        var ec = cfg.getElasticsearch();
        // 通过 RestClient + RestClientTransport 构造 ElasticsearchClient（elasticsearch-java 标准方式）
        org.elasticsearch.client.RestClient restClient = org.elasticsearch.client.RestClient.builder(
                org.apache.http.HttpHost.create("http://" + ec.getHosts())).build();
        co.elastic.clients.json.jackson.JacksonJsonpMapper jsonpMapper =
                new co.elastic.clients.json.jackson.JacksonJsonpMapper();
        co.elastic.clients.transport.rest_client.RestClientTransport transport =
                new co.elastic.clients.transport.rest_client.RestClientTransport(restClient, jsonpMapper);
        co.elastic.clients.elasticsearch.ElasticsearchClient esClient =
                new co.elastic.clients.elasticsearch.ElasticsearchClient(transport);
        registry.register(region, ClientType.ES, new ElasticEsClient(esClient));
        log.info("registered Elasticsearch client for region {}", region);
    }

    private void registerS3(RegionClientRegistry registry, RegionName region, RegionProperties.RegionConfig cfg) {
        if (cfg.getS3() == null) {
            return;
        }
        var sc = cfg.getS3();
        software.amazon.awssdk.services.s3.S3Client s3 = software.amazon.awssdk.services.s3.S3Client.builder()
                .endpointOverride(URI.create(sc.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(sc.getAccessKey(), sc.getSecretKey())))
                .region(Region.AWS_GLOBAL)
                .build();
        registry.register(region, ClientType.S3, new AwsS3Client(s3, sc.getBucket()));
        log.info("registered S3 client for region {}", region);
    }

    private void registerDynamo(RegionClientRegistry registry, RegionName region, RegionProperties.RegionConfig cfg) {
        if (cfg.getDynamodb() == null) {
            return;
        }
        var dc = cfg.getDynamodb();
        software.amazon.awssdk.services.dynamodb.DynamoDbClient dynamo =
                software.amazon.awssdk.services.dynamodb.DynamoDbClient.builder()
                        .endpointOverride(URI.create(dc.getEndpoint()))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(dc.getAccessKey(), dc.getSecretKey())))
                        .region(Region.of(dc.getRegion()))
                        .build();
        registry.register(region, ClientType.DYNAMODB, new AwsDynamoDbClient(dynamo, "tenantId"));
        log.info("registered DynamoDB client for region {}", region);
    }

    private void registerKafka(RegionClientRegistry registry, RegionName region, RegionProperties.RegionConfig cfg) {
        if (cfg.getKafka() == null) {
            return;
        }
        var kc = cfg.getKafka();
        DefaultKafkaProducerFactory<String, String> factory =
                new DefaultKafkaProducerFactory<>(Map.of(
                        org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kc.getBrokers(),
                        org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                        "org.apache.kafka.common.serialization.StringSerializer",
                        org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                        "org.apache.kafka.common.serialization.StringSerializer"));
        KafkaTemplate<String, String> template = new KafkaTemplate<>(factory);
        registry.register(region, ClientType.KAFKA, new SpringKafkaClient(template));
        log.info("registered Kafka client for region {}", region);
    }
}
