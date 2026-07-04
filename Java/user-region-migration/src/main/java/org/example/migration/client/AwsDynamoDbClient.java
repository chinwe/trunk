package org.example.migration.client;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DynamoDB 客户端的 AWS SDK v2 适配器实现。
 *
 * 注意：本类实现的是框架的 org.example.migration.client.DynamoDbClient 接口，
 * 内部持有的 AWS SDK 客户端用全限定名 software.amazon.awssdk.services.dynamodb.DynamoDbClient 区分。
 *
 * queryByTenants: 用 tenantId 作为 key condition 查询。
 * batchPutItems: 批量写入 Item（每条以 Map<String,AttributeValue> 表示）。
 */
public class AwsDynamoDbClient implements org.example.migration.client.DynamoDbClient {

    private final software.amazon.awssdk.services.dynamodb.DynamoDbClient dynamo;
    private final String tenantKeyAttr;

    public AwsDynamoDbClient(software.amazon.awssdk.services.dynamodb.DynamoDbClient dynamo, String tenantKeyAttr) {
        this.dynamo = dynamo;
        this.tenantKeyAttr = tenantKeyAttr;
    }

    @Override
    public List<?> queryByTenants(String tableName, List<String> tenantIds) {
        List<Map<String, AttributeValue>> items = new ArrayList<>();
        for (String tenantId : tenantIds) {
            QueryRequest req = QueryRequest.builder()
                    .tableName(tableName)
                    .keyConditionExpression(tenantKeyAttr + " = :v")
                    .expressionAttributeValues(Map.of(":v", AttributeValue.builder().s(tenantId).build()))
                    .build();
            QueryResponse resp = dynamo.query(req);
            items.addAll(resp.items());
        }
        return items;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void batchPutItems(String tableName, List<?> items) {
        List<WriteRequest> writes = new ArrayList<>();
        for (Object item : items) {
            Map<String, AttributeValue> attrItem = (Map<String, AttributeValue>) item;
            writes.add(WriteRequest.builder()
                    .putRequest(PutRequest.builder().item(attrItem).build())
                    .build());
        }
        dynamo.batchWriteItem(BatchWriteItemRequest.builder()
                .requestItems(Map.of(tableName, writes))
                .build());
    }

    @Override
    public void deleteByTenants(String tableName, List<String> tenantIds) {
        for (String tenantId : tenantIds) {
            dynamo.deleteItem(DeleteItemRequest.builder()
                    .tableName(tableName)
                    .key(Map.of(tenantKeyAttr, AttributeValue.builder().s(tenantId).build()))
                    .build());
        }
    }

    @Override
    public Object raw() {
        return dynamo;
    }
}
