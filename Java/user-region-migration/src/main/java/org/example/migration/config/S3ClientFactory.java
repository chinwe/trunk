package org.example.migration.config;

import org.example.migration.client.AwsS3Client;
import org.example.migration.client.ClientFactory;
import org.example.migration.client.RegionClient;
import org.example.migration.domain.ClientType;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.net.URI;

/**
 * S3 客户端工厂。单实例中间件。
 */
public class S3ClientFactory implements ClientFactory {

    @Override
    public ClientType supportedType() {
        return ClientType.S3;
    }

    @Override
    public RegionClient create(RegionProperties.RegionConfig config, String instanceName) {
        if (config.getS3() == null) {
            return null;
        }
        var sc = config.getS3();
        software.amazon.awssdk.services.s3.S3Client s3 = software.amazon.awssdk.services.s3.S3Client.builder()
                .endpointOverride(URI.create(sc.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(sc.getAccessKey(), sc.getSecretKey())))
                .region(Region.AWS_GLOBAL)
                .build();
        return new AwsS3Client(s3, sc.getBucket());
    }
}
