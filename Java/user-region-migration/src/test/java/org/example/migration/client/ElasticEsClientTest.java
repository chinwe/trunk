package org.example.migration.client;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ElasticEsClient 测试。mock ElasticsearchClient，验证 search/bulk/delete。
 */
class ElasticEsClientTest {

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("searchByTenants: 返回命中文档的 source")
    void shouldSearchByTenants() throws Exception {
        ElasticsearchClient es = mock(ElasticsearchClient.class);
        SearchResponse<Object> resp = mock(SearchResponse.class);
        HitsMetadata<Object> hitsMeta = mock(HitsMetadata.class);
        Hit<Object> hit = mock(Hit.class);
        when(hit.source()).thenReturn(Map.of("k", "v"));
        when(hitsMeta.hits()).thenReturn(List.of(hit));
        when(resp.hits()).thenReturn(hitsMeta);
        when(es.search(any(SearchRequest.class), any())).thenReturn(resp);

        ElasticEsClient client = new ElasticEsClient(es);
        List<?> result = client.searchByTenants("idx", List.of("t1"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(Map.of("k", "v"));
    }

    @Test
    @DisplayName("bulkIndex: 调用 es.bulk 写入文档")
    void shouldBulkIndex() throws Exception {
        ElasticsearchClient es = mock(ElasticsearchClient.class);
        ElasticEsClient client = new ElasticEsClient(es);

        client.bulkIndex("idx", List.of(Map.of("k", "v")));

        // bulk 接收 Builder lambda；用宽松 verify 确认调用发生
        org.mockito.Mockito.verify(es, org.mockito.Mockito.times(1))
                .bulk(org.mockito.ArgumentMatchers.<java.util.function.Function>any());
    }

    @Test
    @DisplayName("deleteByTenants: 调用 es.deleteByQuery")
    void shouldDeleteByTenants() throws Exception {
        ElasticsearchClient es = mock(ElasticsearchClient.class);
        ElasticEsClient client = new ElasticEsClient(es);

        client.deleteByTenants("idx", List.of("t1"));

        org.mockito.Mockito.verify(es, org.mockito.Mockito.times(1))
                .deleteByQuery(org.mockito.ArgumentMatchers.<java.util.function.Function>any());
    }

    @Test
    @DisplayName("raw() 返回底层 ElasticsearchClient")
    void shouldReturnRawEsClient() {
        ElasticsearchClient es = mock(ElasticsearchClient.class);
        assertThat(new ElasticEsClient(es).raw()).isSameAs(es);
    }

    @Test
    @DisplayName("searchByTenants: IOException 时包装为 RuntimeException")
    void shouldWrapIOExceptionOnSearch() throws Exception {
        ElasticsearchClient es = mock(ElasticsearchClient.class);
        when(es.search(any(SearchRequest.class), any())).thenThrow(new java.io.IOException("conn refused"));

        ElasticEsClient client = new ElasticEsClient(es);
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        client.searchByTenants("idx", List.of("t1")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("search failed");
    }

    @Test
    @DisplayName("bulkIndex: IOException 时包装为 RuntimeException")
    void shouldWrapIOExceptionOnBulk() throws Exception {
        ElasticsearchClient es = mock(ElasticsearchClient.class);
        org.mockito.Mockito.doThrow(new java.io.IOException("bulk fail"))
                .when(es).bulk(org.mockito.ArgumentMatchers.<java.util.function.Function>any());

        ElasticEsClient client = new ElasticEsClient(es);
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        client.bulkIndex("idx", List.of(Map.of("k", "v"))))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("bulk index failed");
    }

    @Test
    @DisplayName("deleteByTenants: IOException 时包装为 RuntimeException")
    void shouldWrapIOExceptionOnDelete() throws Exception {
        ElasticsearchClient es = mock(ElasticsearchClient.class);
        org.mockito.Mockito.doThrow(new java.io.IOException("del fail"))
                .when(es).deleteByQuery(org.mockito.ArgumentMatchers.<java.util.function.Function>any());

        ElasticEsClient client = new ElasticEsClient(es);
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        client.deleteByTenants("idx", List.of("t1")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("deleteByQuery failed");
    }
}
