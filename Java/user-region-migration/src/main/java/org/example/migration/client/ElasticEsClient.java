package org.example.migration.client;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Elasticsearch 客户端的 elasticsearch-java 适配器实现。
 *
 * searchByTenants: 用 terms query 按 tenantIds 过滤。
 * deleteByTenants: 按 tenantIds 删除文档（通过 deleteByQuery）。
 */
public class ElasticEsClient implements EsClient {

    private final ElasticsearchClient es;

    public ElasticEsClient(ElasticsearchClient es) {
        this.es = es;
    }

    @Override
    public List<?> searchByTenants(String index, List<String> tenantIds) {
        try {
            Query query = TermsQuery.of(t -> t
                    .field("tenantId")
                    .terms(v -> v.value(tenantIds.stream().map(co.elastic.clients.elasticsearch._types.FieldValue::of).toList()))
            )._toQuery();
            SearchRequest req = SearchRequest.of(s -> s.index(index).query(query));
            SearchResponse<Object> resp = es.search(req, Object.class);
            List<Object> sources = new ArrayList<>();
            for (Hit<Object> hit : resp.hits().hits()) {
                sources.add(hit.source());
            }
            return sources;
        } catch (IOException e) {
            throw new RuntimeException("elasticsearch search failed", e);
        }
    }

    @Override
    public void bulkIndex(String index, List<?> documents) {
        try {
            es.bulk(b -> {
                for (int i = 0; i < documents.size(); i++) {
                    final int idx = i;
                    b.operations(op -> op.index(d -> d.index(index).id(String.valueOf(idx)).document(documents.get(idx))));
                }
                return b;
            });
        } catch (IOException e) {
            throw new RuntimeException("elasticsearch bulk index failed", e);
        }
    }

    @Override
    public void deleteByTenants(String index, List<String> tenantIds) {
        try {
            Query query = TermsQuery.of(t -> t
                    .field("tenantId")
                    .terms(v -> v.value(tenantIds.stream().map(co.elastic.clients.elasticsearch._types.FieldValue::of).toList()))
            )._toQuery();
            es.deleteByQuery(d -> d.index(index).query(query));
        } catch (IOException e) {
            throw new RuntimeException("elasticsearch deleteByQuery failed", e);
        }
    }

    @Override
    public Object raw() {
        return es;
    }
}
