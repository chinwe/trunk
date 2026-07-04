package org.example.migration.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SpringRedisClient 测试。mock RedisTemplate，验证 scan/get/set/delete 委托。
 */
class SpringRedisClientTest {

    @SuppressWarnings("unchecked")
    private RedisTemplate<String, String> redisTemplate() {
        return mock(RedisTemplate.class);
    }

    @Test
    @DisplayName("scanKeysByTenants: 用 tenantId 替换 {tenant} 占位,聚合所有租户的 key")
    void shouldScanKeysByTenants() {
        RedisTemplate<String, String> redis = redisTemplate();
        Cursor<String> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn("session:t1:abc", "session:t1:def");
        when(redis.scan(any(ScanOptions.class))).thenReturn(cursor);

        SpringRedisClient client = new SpringRedisClient(redis);
        List<String> keys = client.scanKeysByTenants("session:{tenant}:*", List.of("t1"));

        assertThat(keys).containsExactly("session:t1:abc", "session:t1:def");
    }

    @Test
    @DisplayName("scanKeysByTenants: 多个租户时聚合所有 key")
    void shouldAggregateKeysAcrossTenants() {
        RedisTemplate<String, String> redis = redisTemplate();
        Cursor<String> c1 = mock(Cursor.class);
        when(c1.hasNext()).thenReturn(true, false);
        when(c1.next()).thenReturn("k:t1");
        Cursor<String> c2 = mock(Cursor.class);
        when(c2.hasNext()).thenReturn(true, false);
        when(c2.next()).thenReturn("k:t2");
        when(redis.scan(any(ScanOptions.class))).thenReturn(c1, c2);

        SpringRedisClient client = new SpringRedisClient(redis);
        List<String> keys = client.scanKeysByTenants("k:{tenant}", List.of("t1", "t2"));

        assertThat(keys).containsExactly("k:t1", "k:t2");
    }

    @Test
    @DisplayName("get/set/delete 委托 RedisTemplate")
    void shouldDelegateBasicOps() {
        RedisTemplate<String, String> redis = redisTemplate();
        org.springframework.data.redis.core.ValueOperations<String, String> valueOps = mock(org.springframework.data.redis.core.ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("key1")).thenReturn("value1");

        SpringRedisClient client = new SpringRedisClient(redis);
        assertThat(client.get("key1")).isEqualTo("value1");

        client.set("key1", "value1");
        client.delete("key1");

        verify(valueOps).set("key1", "value1");
        verify(redis).delete("key1");
    }

    @Test
    @DisplayName("raw() 返回底层 RedisTemplate")
    void shouldReturnRawRedisTemplate() {
        RedisTemplate<String, String> redis = redisTemplate();
        assertThat(new SpringRedisClient(redis).raw()).isSameAs(redis);
    }
}
