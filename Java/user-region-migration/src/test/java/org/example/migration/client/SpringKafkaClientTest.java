package org.example.migration.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SpringKafkaClient 测试。mock KafkaTemplate，验证 send。
 */
class SpringKafkaClientTest {

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("send: 委托 KafkaTemplate.send(topic, message)")
    void shouldDelegateSendToKafkaTemplate() {
        KafkaTemplate<String, String> kafka = mock(KafkaTemplate.class);
        when(kafka.send(eq("topic1"), eq("hello")))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        SpringKafkaClient client = new SpringKafkaClient(kafka);
        client.send("topic1", "hello");

        verify(kafka).send("topic1", "hello");
    }

    @Test
    @DisplayName("raw() 返回底层 KafkaTemplate")
    void shouldReturnRawKafkaTemplate() {
        KafkaTemplate<String, String> kafka = mock(KafkaTemplate.class);
        assertThat(new SpringKafkaClient(kafka).raw()).isSameAs(kafka);
    }
}
