package com.learn.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

/**
 * @author chinwe
 */
public class DocDelete {
    public static void main(String[] args) throws IOException {

        // 创建ES客户端对象
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        // 删除数据
        final DeleteRequest request = new DeleteRequest();
        request.index("user").id("1001");

        final DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);

        System.out.println(response.getResult());

        // 关闭客户端连接
        client.close();
    }
}
