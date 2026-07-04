package org.example.migration.engine;

import org.example.migration.client.KafkaClient;
import org.example.migration.client.RegionClientRegistry;
import org.example.migration.domain.ClientType;
import org.example.migration.domain.RegionName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * KafkaMigrationNotifier 测试：验证向源区发迁出通知、目标区发迁入通知。
 */
class KafkaMigrationNotifierTest {

    @Test
    @DisplayName("向源区发 migrated-out,目标区发 migrated-in")
    void shouldNotifyBothRegions() {
        RegionClientRegistry registry = mock(RegionClientRegistry.class);
        KafkaClient sgKafka = mock(KafkaClient.class);
        KafkaClient mmKafka = mock(KafkaClient.class);
        when(registry.client(eq(RegionName.SINGAPORE), eq(ClientType.KAFKA), eq(KafkaClient.class))).thenReturn(sgKafka);
        when(registry.client(eq(RegionName.MYANMAR), eq(ClientType.KAFKA), eq(KafkaClient.class))).thenReturn(mmKafka);

        KafkaMigrationNotifier notifier = new KafkaMigrationNotifier(registry);
        notifier.notify(RegionName.SINGAPORE, RegionName.MYANMAR, "key", "payload");

        verify(sgKafka).send(eq("singapore-migration-notify"), eq("migrated-out:payload"));
        verify(mmKafka).send(eq("myanmar-migration-notify"), eq("migrated-in:payload"));
    }

    @Test
    @DisplayName("某 region 无 Kafka 客户端时跳过该侧通知(不抛异常)")
    void shouldSkipWhenNoKafkaClient() {
        RegionClientRegistry registry = mock(RegionClientRegistry.class);
        when(registry.client(any(RegionName.class), eq(ClientType.KAFKA), eq(KafkaClient.class)))
                .thenThrow(new IllegalArgumentException("no client registered"));

        KafkaMigrationNotifier notifier = new KafkaMigrationNotifier(registry);
        // 不抛异常：被内部 catch 住
        notifier.notify(RegionName.SINGAPORE, RegionName.MYANMAR, "key", "payload");
    }
}
