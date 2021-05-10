package com.learn.es;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;

import java.io.IOException;

/**
 * @author chinwe
 */
public class IndexCreate {
    public static void main(String[] args) throws IOException {

        // 创建ES客户端对象
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        // 创建索引
        CreateIndexRequest user = new CreateIndexRequest("user");
        final CreateIndexResponse createIndexResponse = client.indices().create(user, RequestOptions.DEFAULT);

        final boolean acknowledged = createIndexResponse.isAcknowledged();
        System.out.println("索引操作: " + acknowledged);

        // 关闭客户端连接
        client.close();
    }
}
