package org.example.migration.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AwsDynamoDbClient 测试。mock AWS DynamoDbClient，验证 query/batchPut/delete。
 */
class AwsDynamoDbClientTest {

    @Test
    @DisplayName("queryByTenants: 对每个租户执行 query 并聚合结果")
    void shouldQueryByTenants() {
        DynamoDbClient dynamo = mock(DynamoDbClient.class);
        QueryResponse resp = mock(QueryResponse.class);
        Map<String, AttributeValue> item = Map.of("tenantId", AttributeValue.builder().s("t1").build());
        when(resp.items()).thenReturn(List.of(item));
        when(dynamo.query(any(QueryRequest.class))).thenReturn(resp);

        AwsDynamoDbClient client = new AwsDynamoDbClient(dynamo, "tenantId");
        List<?> result = client.queryByTenants("mytable", List.of("t1"));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("batchPutItems: 构造 BatchWriteItemRequest 写入")
    void shouldBatchPutItems() {
        DynamoDbClient dynamo = mock(DynamoDbClient.class);
        AwsDynamoDbClient client = new AwsDynamoDbClient(dynamo, "tenantId");

        Map<String, AttributeValue> item = Map.of("id", AttributeValue.builder().s("x1").build());
        client.batchPutItems("mytable", List.of(item));

        verify(dynamo).batchWriteItem(any(BatchWriteItemRequest.class));
    }

    @Test
    @DisplayName("deleteByTenants: 对每个租户删除")
    void shouldDeleteByTenants() {
        DynamoDbClient dynamo = mock(DynamoDbClient.class);
        AwsDynamoDbClient client = new AwsDynamoDbClient(dynamo, "tenantId");

        client.deleteByTenants("mytable", List.of("t1", "t2"));

        // 每个租户删除一次，2 个租户调 2 次
        verify(dynamo, org.mockito.Mockito.times(2)).deleteItem(any(DeleteItemRequest.class));
    }

    @Test
    @DisplayName("raw() 返回底层 AWS DynamoDbClient")
    void shouldReturnRawDynamoDbClient() {
        DynamoDbClient dynamo = mock(DynamoDbClient.class);
        assertThat(new AwsDynamoDbClient(dynamo, "tenantId").raw()).isSameAs(dynamo);
    }
}
