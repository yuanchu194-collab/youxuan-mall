package com.youxuan.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 低阶 REST 客户端配置，阶段 8 只做手动导入和搜索。
 */
@Configuration
public class ElasticsearchConfig {

    @Bean
    public RestClient elasticsearchRestClient(@Value("${youxuan.elasticsearch.uris:http://localhost:9200}") String uris) {
        HttpHost[] hosts = java.util.Arrays.stream(uris.split(","))
                .map(String::trim)
                .filter(uri -> !uri.isEmpty())
                .map(HttpHost::create)
                .toArray(HttpHost[]::new);
        return RestClient.builder(hosts).build();
    }
}
