package com.learn.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author chinwe
 */
public class DocBatchInsert {
    public static void main(String[] args) throws IOException {

        // 创建ES客户端对象
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        // 批量插入数据
        final BulkRequest bulkRequest = new BulkRequest();

        bulkRequest.add(new IndexRequest().index("user").id("1001").source(XContentType.JSON, "name", "zhangsan", "age", 30, "sex", "Male"));
        bulkRequest.add(new IndexRequest().index("user").id("1002").source(XContentType.JSON, "name", "lisi", "age", 32, "sex", "FeMale"));
        bulkRequest.add(new IndexRequest().index("user").id("1003").source(XContentType.JSON, "name", "wangwu", "age", 35, "sex", "Male"));
        bulkRequest.add(new IndexRequest().index("user").id("1004").source(XContentType.JSON, "name", "zhaoliu", "age", 31, "sex", "FeMale"));
        bulkRequest.add(new IndexRequest().index("user").id("1005").source(XContentType.JSON, "name", "qianqi", "age", 47, "sex", "Male"));
        bulkRequest.add(new IndexRequest().index("user").id("1006").source(XContentType.JSON, "name", "zhouba", "age", 50, "sex", "Male"));

        final BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

        System.out.println(bulkResponse.getTook());
        System.out.println(Arrays.toString(bulkResponse.getItems()));

        // 关闭客户端连接
        client.close();
    }
}
