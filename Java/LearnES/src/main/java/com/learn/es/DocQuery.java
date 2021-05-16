package com.learn.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;

/**
 * @author chinwe
 */
public class DocQuery {
    public static void main(String[] args) throws IOException {

        // 创建ES客户端对象
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        matchQueryA(client);

        // 关闭客户端连接
        client.close();
    }

    private static void matchAllQuery(RestHighLevelClient client) throws IOException {
        // 全量查询数据
        final SearchRequest request = new SearchRequest();
        request.indices("user");

        final SearchSourceBuilder builder = new SearchSourceBuilder().query(QueryBuilders.matchAllQuery());
        request.source(builder);

        final SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        final SearchHits hits = response.getHits();
        System.out.println(hits.getTotalHits());
        System.out.println(response.getTook());
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsString());
        }
    }

    private static void matchQuery(RestHighLevelClient client) throws IOException {
        // 查询数据
        final SearchRequest request = new SearchRequest();
        request.indices("user");

        final SearchSourceBuilder builder = new SearchSourceBuilder().query(QueryBuilders.matchAllQuery());

        // 分页
        builder.from(1);
        builder.size(2);

        // 排序
        builder.sort("age", SortOrder.ASC);

        // 过滤字段
        String[] excludes = { "age" };
        String[] includes = {};
        builder.fetchSource(includes, excludes);

        request.source(builder);

        final SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        final SearchHits hits = response.getHits();
        System.out.println(hits.getTotalHits());
        System.out.println(response.getTook());
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsString());
        }
    }

    private static void matchQueryA(RestHighLevelClient client) throws IOException {
        // 查询数据
        final SearchRequest request = new SearchRequest();
        request.indices("user");

        final BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        boolQueryBuilder.should(QueryBuilders.matchQuery("age", 30));
        boolQueryBuilder.should(QueryBuilders.matchQuery("age", 32));
        boolQueryBuilder.should(QueryBuilders.matchQuery("sex", "Male"));

        final SearchSourceBuilder builder = new SearchSourceBuilder().query(boolQueryBuilder);
        request.source(builder);

        final SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        final SearchHits hits = response.getHits();
        System.out.println(hits.getTotalHits());
        System.out.println(response.getTook());
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsString());
        }
    }
}
