package org.example.migration.client;

/**
 * Kafka 客户端。切流通知发送（迁出/迁入通知）。
 * 与其他中间件不同，Kafka 主要用于发送通知而非数据搬运。
 */
public non-sealed interface KafkaClient extends RegionClient {

    /**
     * 发送迁移通知。
     *
     * @param topic   目标 topic（由配置的 topic-prefix 拼接）
     * @param message 通知消息体
     */
    void send(String topic, String message);
}
