package org.example.migration.config;

import org.example.migration.client.AwsDynamoDbClient;
import org.example.migration.client.ClientFactory;
import org.example.migration.client.RegionClient;
import org.example.migration.domain.ClientType;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.net.URI;

/**
 * DynamoDB 客户端工厂。单实例中间件。
 */
public class DynamoDbClientFactory implements ClientFactory {

    @Override
    public ClientType supportedType() {
        return ClientType.DYNAMODB;
    }

    @Override
    public RegionClient create(RegionProperties.RegionConfig config, String instanceName) {
        if (config.getDynamodb() == null) {
            return null;
        }
        var dc = config.getDynamodb();
        software.amazon.awssdk.services.dynamodb.DynamoDbClient dynamo =
                software.amazon.awssdk.services.dynamodb.DynamoDbClient.builder()
                        .endpointOverride(URI.create(dc.getEndpoint()))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(dc.getAccessKey(), dc.getSecretKey())))
                        .region(Region.of(dc.getRegion()))
                        .build();
        return new AwsDynamoDbClient(dynamo, "tenantId");
    }
}
