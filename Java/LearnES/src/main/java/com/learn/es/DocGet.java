package com.learn.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

/**
 * @author chinwe
 */
public class DocGet {
    public static void main(String[] args) throws IOException {

        // 创建ES客户端对象
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        // 查询数据
        final GetRequest request = new GetRequest();
        request.index("user").id("1001");

        final GetResponse response = client.get(request, RequestOptions.DEFAULT);

        System.out.println(response.getSourceAsString());

        // 关闭客户端连接
        client.close();
    }
}
