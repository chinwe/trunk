package org.example.migration.config;

import org.example.migration.client.ClientFactory;
import org.example.migration.client.RegionClient;
import org.example.migration.client.SpringKafkaClient;
import org.example.migration.domain.ClientType;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

/**
 * Kafka 客户端工厂。单实例中间件（主要用于通知，非数据搬运）。
 */
public class KafkaClientFactory implements ClientFactory {

    @Override
    public ClientType supportedType() {
        return ClientType.KAFKA;
    }

    @Override
    public RegionClient create(RegionProperties.RegionConfig config, String instanceName) {
        if (config.getKafka() == null) {
            return null;
        }
        var kc = config.getKafka();
        DefaultKafkaProducerFactory<String, String> factory =
                new DefaultKafkaProducerFactory<>(Map.of(
                        org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kc.getBrokers(),
                        org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                        "org.apache.kafka.common.serialization.StringSerializer",
                        org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                        "org.apache.kafka.common.serialization.StringSerializer"));
        KafkaTemplate<String, String> template = new KafkaTemplate<>(factory);
        return new SpringKafkaClient(template);
    }
}
