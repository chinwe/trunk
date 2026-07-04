package org.example.migration.client;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis 客户端的 Spring Data Redis 适配器实现。
 *
 * scanKeysByTenants: 对每个租户用 SCAN 命令匹配 keyPattern（用 tenantId 替换占位）。
 */
public class SpringRedisClient implements RedisClient {

    private final RedisTemplate<String, String> redisTemplate;

    public SpringRedisClient(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<String> scanKeysByTenants(String keyPattern, List<String> tenantIds) {
        List<String> keys = new ArrayList<>();
        for (String tenantId : tenantIds) {
            String pattern = keyPattern.replace("{tenant}", tenantId);
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
            try (Cursor<String> cursor = redisTemplate.scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(cursor.next());
                }
            }
        }
        return keys;
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public Object raw() {
        return redisTemplate;
    }
}
