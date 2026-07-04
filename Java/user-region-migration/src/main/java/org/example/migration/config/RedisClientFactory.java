package org.example.migration.config;

import org.example.migration.client.ClientFactory;
import org.example.migration.client.RegionClient;
import org.example.migration.client.SpringRedisClient;
import org.example.migration.domain.ClientType;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis 客户端工厂。支持多实例（同一 region 可配多个 Redis 实例）。
 */
public class RedisClientFactory implements ClientFactory {

    @Override
    public ClientType supportedType() {
        return ClientType.REDIS;
    }

    @Override
    public RegionClient create(RegionProperties.RegionConfig config, String instanceName) {
        if (config.getRedis() == null || !config.getRedis().containsKey(instanceName)) {
            return null;
        }
        RegionProperties.RedisConfig rc = config.getRedis().get(instanceName);
        RedisStandaloneConfiguration redisCfg = new RedisStandaloneConfiguration(rc.getHost(), rc.getPort());
        if (rc.getPassword() != null) {
            redisCfg.setPassword(rc.getPassword());
        }
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisCfg);
        factory.afterPropertiesSet();
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.afterPropertiesSet();
        return new SpringRedisClient(template);
    }

    @Override
    public boolean supportsMultiInstance() {
        return true;
    }
}
