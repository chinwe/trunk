package org.example.migration.example;

import org.example.migration.client.KafkaClient;
import org.example.migration.client.RedisClient;
import org.example.migration.domain.ClientType;
import org.example.migration.domain.RegionName;
import org.example.migration.spi.MigrationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserCutoverAction 测试：验证踢登录(删 Redis session) + 发 Kafka 通知。
 * Redis 用四参 ctx.client(region, REDIS, "session", RedisClient.class)，
 * Kafka 保持三参（单实例中间件）。
 */
class UserCutoverActionTest {

    @Test
    @DisplayName("evict: 删除目标区 Redis session,发 Kafka 通知")
    void shouldDeleteSessionsAndNotify() {
        RedisClient redis = mock(RedisClient.class);
        KafkaClient kafka = mock(KafkaClient.class);
        when(redis.scanKeysByTenants(any(), anyList())).thenReturn(List.of("session:t1:abc"));
        MigrationContext ctx = mock(MigrationContext.class);
        when(ctx.targetRegion()).thenReturn(RegionName.MYANMAR);
        when(ctx.client(RegionName.MYANMAR, ClientType.REDIS, "session", RedisClient.class)).thenReturn(redis);
        when(ctx.client(RegionName.MYANMAR, ClientType.KAFKA, KafkaClient.class)).thenReturn(kafka);

        UserCutoverAction action = new UserCutoverAction();
        action.evict(ctx, List.of("t1"), "p", "b");

        verify(redis).delete("session:t1:abc");
        verify(kafka).send(eq("migration-notify"), any());
    }
}
