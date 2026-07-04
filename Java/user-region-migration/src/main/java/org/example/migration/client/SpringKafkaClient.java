package org.example.migration.client;

import org.springframework.kafka.core.KafkaTemplate;

/**
 * Kafka 客户端的 Spring Kafka 适配器实现。
 * 主要用于切流时发送迁出/迁入通知。
 */
public class SpringKafkaClient implements KafkaClient {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public SpringKafkaClient(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }

    @Override
    public Object raw() {
        return kafkaTemplate;
    }
}
