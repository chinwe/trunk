package org.example.migration.config;

import org.example.migration.client.ClientFactory;
import org.example.migration.client.RegionClient;
import org.example.migration.client.RegionClientRegistry;
import org.example.migration.domain.RegionName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * 客户端自动装配：根据 RegionProperties 为每个 region 的每个中间件创建客户端并注册。
 * 加新 region 只改 yml，零 Java 改动。
 *
 * 客户端创建通过 {@link ClientFactory} SPI 委托——各中间件只需实现 ClientFactory
 * 并声明为 Spring Bean 即可自动参与注册，无需修改本类。
 *
 * 客户端创建遵循"按需创建"原则：某 region 某中间件未配置则跳过（不报错）。
 */
@Configuration
@EnableConfigurationProperties({ RegionProperties.class, MigrationProperties.class })
public class RegionClientAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RegionClientAutoConfiguration.class);

    // ── 默认 ClientFactory Bean（业务可通过声明同类型 Bean 覆盖）──

    @Bean
    @ConditionalOnMissingBean
    public MySqlClientFactory mySqlClientFactory() {
        return new MySqlClientFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisClientFactory redisClientFactory() {
        return new RedisClientFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public EsClientFactory esClientFactory() {
        return new EsClientFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public S3ClientFactory s3ClientFactory() {
        return new S3ClientFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamoDbClientFactory dynamoDbClientFactory() {
        return new DynamoDbClientFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public KafkaClientFactory kafkaClientFactory() {
        return new KafkaClientFactory();
    }

    // ── 核心注册逻辑（SPI 驱动）──

    @Bean
    @ConditionalOnMissingBean
    public RegionClientRegistry regionClientRegistry(RegionProperties props, List<ClientFactory> factories) {
        RegionClientRegistry registry = new RegionClientRegistry();
        if (props.getRegions() == null || factories.isEmpty()) {
            return registry;
        }
        props.getRegions().forEach((regionName, cfg) -> {
            if (cfg == null) {
                return;
            }
            RegionName region = RegionName.of(regionName);
            for (ClientFactory factory : factories) {
                registerFromFactory(registry, region, cfg, factory);
            }
        });
        return registry;
    }

    /** 从单个 ClientFactory 注册客户端（处理多实例与单实例差异） */
    private void registerFromFactory(RegionClientRegistry registry, RegionName region,
                                     RegionProperties.RegionConfig cfg, ClientFactory factory) {
        if (factory.supportsMultiInstance()) {
            // 多实例中间件（MySQL/Redis）：遍历配置 Map 的 key 作为实例名
            Map<String, ?> instances = getMultiInstanceConfig(cfg, factory.supportedType());
            if (instances == null) {
                return;
            }
            instances.forEach((instanceName, instanceCfg) -> {
                RegionClient client = factory.create(cfg, instanceName);
                if (client != null) {
                    registry.register(region, factory.supportedType(), instanceName, client);
                    log.info("registered {} client [{}] for region {}",
                            factory.supportedType(), instanceName, region);
                }
            });
        } else {
            // 单实例中间件（ES/S3/DynamoDB/Kafka）：instance 固定 "default"
            RegionClient client = factory.create(cfg, RegionClientRegistry.DEFAULT_INSTANCE);
            if (client != null) {
                registry.register(region, factory.supportedType(), client);
                log.info("registered {} client for region {}", factory.supportedType(), region);
            }
        }
    }

    /** 根据 ClientType 获取多实例配置 Map */
    private Map<String, ?> getMultiInstanceConfig(RegionProperties.RegionConfig cfg,
                                                   org.example.migration.domain.ClientType type) {
        return switch (type) {
            case MYSQL -> cfg.getMysql();
            case REDIS -> cfg.getRedis();
            default -> null; // 非多实例类型不应走到这里
        };
    }
}
