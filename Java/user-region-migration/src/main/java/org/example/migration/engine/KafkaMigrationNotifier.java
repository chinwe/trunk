package org.example.migration.engine;

import org.example.migration.client.KafkaClient;
import org.example.migration.client.RegionClientRegistry;
import org.example.migration.domain.ClientType;
import org.example.migration.domain.RegionName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kafka 迁移通知器。切流后向源区与目标区分别发送迁出/迁入通知。
 *
 * 通知 topic 约定：{region-topic-prefix}-migration-notify。
 * 通知 key：source 侧发 "migrated-out"，target 侧发 "migrated-in"。
 */
public class KafkaMigrationNotifier implements MigrationNotifier {

    private static final Logger log = LoggerFactory.getLogger(KafkaMigrationNotifier.class);

    private final RegionClientRegistry registry;

    public KafkaMigrationNotifier(RegionClientRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void notify(RegionName sourceRegion, RegionName targetRegion, String payload) {
        // 向源区发迁出通知
        sendTo(sourceRegion, "migrated-out", payload);
        // 向目标区发迁入通知
        sendTo(targetRegion, "migrated-in", payload);
    }

    private void sendTo(RegionName region, String key, String payload) {
        KafkaClient kafka;
        try {
            kafka = registry.client(region, ClientType.KAFKA, KafkaClient.class);
        } catch (IllegalArgumentException e) {
            log.warn("no Kafka client for region {}, skip notification {}: {}", region, key, e.getMessage());
            return;
        }
        kafka.send(region.value() + "-migration-notify", key + ":" + payload);
        log.info("sent migration notification to region {}: {}", region, key);
    }
}
