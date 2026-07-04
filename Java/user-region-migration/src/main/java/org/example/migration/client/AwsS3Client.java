package org.example.migration.client;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.ArrayList;
import java.util.List;

/**
 * S3 客户端的 AWS SDK v2 适配器实现。
 *
 * listKeysByTenants: 列出 bucket 中匹配 prefix（{tenant} 占位用 tenantId 替换）的对象 key。
 * copyObject: 从当前 bucket 拷贝到目标客户端（同为 AwsS3Client）的 bucket。
 * exists: 用 HeadObject 探测，NoSuchKey 时返回 false。
 */
public class AwsS3Client implements S3Client {

    private final software.amazon.awssdk.services.s3.S3Client s3;
    private final String bucket;

    public AwsS3Client(software.amazon.awssdk.services.s3.S3Client s3, String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    @Override
    public List<String> listKeysByTenants(String prefix, List<String> tenantIds) {
        List<String> keys = new ArrayList<>();
        for (String tenantId : tenantIds) {
            String fullPrefix = prefix.replace("{tenant}", tenantId);
            ListObjectsV2Request req = ListObjectsV2Request.builder()
                    .bucket(bucket).prefix(fullPrefix).build();
            ListObjectsV2Response resp = s3.listObjectsV2(req);
            for (S3Object obj : resp.contents()) {
                keys.add(obj.key());
            }
        }
        return keys;
    }

    @Override
    public void copyObject(String sourceKey, S3Client targetClient, String targetKey) {
        CopyObjectRequest req = CopyObjectRequest.builder()
                .sourceBucket(bucket).sourceKey(sourceKey)
                .destinationBucket(targetClient.getBucket()).destinationKey(targetKey).build();
        s3.copyObject(req);
    }

    @Override
    public String getBucket() {
        return bucket;
    }

    @Override
    public boolean exists(String key) {
        HeadObjectRequest req = HeadObjectRequest.builder().bucket(bucket).key(key).build();
        try {
            s3.headObject(req);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    public void putObject(String key, byte[] content) {
        s3.putObject(b -> b.bucket(bucket).key(key), RequestBody.fromBytes(content));
    }

    @Override
    public void delete(String key) {
        DeleteObjectRequest req = DeleteObjectRequest.builder().bucket(bucket).key(key).build();
        s3.deleteObject(req);
    }

    @Override
    public Object raw() {
        return s3;
    }
}
