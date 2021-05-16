package com.learn.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;

/**
 * @author chinwe
 */
public class DocInsert {
    public static void main(String[] args) throws IOException {

        // 创建ES客户端对象
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        // 插入数据
        final User user = new User();
        user.setName("zhangsan");
        user.setSex("Male");
        user.setAge(30);

        final ObjectMapper objectMapper = new ObjectMapper();
        final String userJson = objectMapper.writeValueAsString(user);

        IndexRequest indexRequest = new IndexRequest();
        indexRequest.index("user").id("1001");
        indexRequest.source(userJson, XContentType.JSON);

        final IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);

        System.out.println(indexResponse.getResult());

        // 关闭客户端连接
        client.close();
    }
}
