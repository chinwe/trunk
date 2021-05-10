package com.learn.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;

import java.io.IOException;

/**
 * @author chinwe
 */
public class IndexDelete {
    public static void main(String[] args) throws IOException {

        // 创建ES客户端对象
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        // 索引删除
        final DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("user");
        final AcknowledgedResponse acknowledgedResponse = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);

        final boolean acknowledged = acknowledgedResponse.isAcknowledged();
        System.out.println("索引删除: " + acknowledged);

        // 关闭客户端连接
        client.close();
    }
}
