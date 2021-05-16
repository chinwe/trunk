package com.learn.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;

/**
 * @author chinwe
 */
public class DocUpdate {
    public static void main(String[] args) throws IOException {

        // 创建ES客户端对象
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        // 修改数据
        final UpdateRequest request = new UpdateRequest();
        request.index("user").id("1001");
        request.doc(XContentType.JSON, "sex", "Male");

        final UpdateResponse response = client.update(request, RequestOptions.DEFAULT);

        System.out.println(response.getResult());

        // 关闭客户端连接
        client.close();
    }
}
