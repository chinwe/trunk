package com.learn.es;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;

import java.io.IOException;

/**
 * @author chinwe
 */
public class IndexSearch {
    public static void main(String[] args) throws IOException {

        // 创建ES客户端对象
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        // 查询索引
        final GetIndexRequest getIndexRequest = new GetIndexRequest("user");
        final GetIndexResponse getIndexResponse = client.indices().get(getIndexRequest, RequestOptions.DEFAULT);

        System.out.println("查询索引: ");
        System.out.println(getIndexResponse.getAliases());
        System.out.println(getIndexResponse.getMappings());
        System.out.println(getIndexResponse.getSettings());

        // 关闭客户端连接
        client.close();
    }
}
