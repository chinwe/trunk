package org.example.migration.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AwsS3Client 测试。mock AWS S3Client，验证 list/copy/exists/put/delete。
 */
class AwsS3ClientTest {

    @Test
    @DisplayName("listKeysByTenants: 按 tenant 替换 {tenant} 占位列出对象 key")
    void shouldListKeysByTenants() {
        S3Client s3 = mock(S3Client.class);
        ListObjectsV2Response resp = mock(ListObjectsV2Response.class);
        when(resp.contents()).thenReturn(List.of(
                S3Object.builder().key("files/t1/a.txt").build(),
                S3Object.builder().key("files/t1/b.txt").build()));
        when(s3.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(resp);

        AwsS3Client client = new AwsS3Client(s3, "mybucket");
        List<String> keys = client.listKeysByTenants("files/{tenant}/", List.of("t1"));

        assertThat(keys).containsExactly("files/t1/a.txt", "files/t1/b.txt");
    }

    @Test
    @DisplayName("copyObject: 从源 bucket 拷贝到目标 bucket")
    void shouldCopyObjectToTargetBucket() {
        S3Client src = mock(S3Client.class);
        S3Client tgt = mock(S3Client.class);
        AwsS3Client srcClient = new AwsS3Client(src, "src-bucket");
        AwsS3Client tgtClient = new AwsS3Client(tgt, "tgt-bucket");

        srcClient.copyObject("key1", tgtClient, "key2");

        verify(src).copyObject(any(CopyObjectRequest.class));
    }

    @Test
    @DisplayName("exists: HeadObject 成功返回 true")
    void shouldReturnTrueWhenObjectExists() {
        S3Client s3 = mock(S3Client.class);
        when(s3.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().build());

        AwsS3Client client = new AwsS3Client(s3, "bucket");
        assertThat(client.exists("key")).isTrue();
    }

    @Test
    @DisplayName("exists: NoSuchKeyException 时返回 false")
    void shouldReturnFalseWhenNoSuchKey() {
        S3Client s3 = mock(S3Client.class);
        when(s3.headObject(any(HeadObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());

        AwsS3Client client = new AwsS3Client(s3, "bucket");
        assertThat(client.exists("key")).isFalse();
    }

    @Test
    @DisplayName("delete 调用 s3.deleteObject")
    void shouldDeleteObject() {
        S3Client s3 = mock(S3Client.class);
        AwsS3Client client = new AwsS3Client(s3, "bucket");

        client.delete("key");

        verify(s3).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("raw() 返回底层 AWS S3Client")
    void shouldReturnRawS3Client() {
        S3Client s3 = mock(S3Client.class);
        assertThat(new AwsS3Client(s3, "bucket").raw()).isSameAs(s3);
    }

    @Test
    @DisplayName("putObject: 调用 s3.putObject 上传内容")
    void shouldPutObject() {
        S3Client s3 = mock(S3Client.class);
        AwsS3Client client = new AwsS3Client(s3, "bucket");

        client.putObject("key", new byte[]{1, 2, 3});

        // putObject 接收 Consumer<PutObjectRequest.Builder> + RequestBody 重载
        org.mockito.Mockito.verify(s3, org.mockito.Mockito.times(1))
                .putObject(any(java.util.function.Consumer.class),
                        any(software.amazon.awssdk.core.sync.RequestBody.class));
    }
}
