package org.example.migration.config;

import org.example.migration.client.ClientFactory;
import org.example.migration.client.ElasticEsClient;
import org.example.migration.client.RegionClient;
import org.example.migration.domain.ClientType;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;

/**
 * Elasticsearch 客户端工厂。单实例中间件。
 */
public class EsClientFactory implements ClientFactory {

    @Override
    public ClientType supportedType() {
        return ClientType.ES;
    }

    @Override
    public RegionClient create(RegionProperties.RegionConfig config, String instanceName) {
        if (config.getElasticsearch() == null) {
            return null;
        }
        var ec = config.getElasticsearch();
        RestClient restClient = RestClient.builder(
                HttpHost.create("http://" + ec.getHosts())).build();
        JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper();
        RestClientTransport transport = new RestClientTransport(restClient, jsonpMapper);
        co.elastic.clients.elasticsearch.ElasticsearchClient esClient =
                new co.elastic.clients.elasticsearch.ElasticsearchClient(transport);
        return new ElasticEsClient(esClient);
    }
}
