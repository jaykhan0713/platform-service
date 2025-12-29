package com.jay.template.bootstrap.outbound.http.client.rest.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import com.jay.template.core.port.outbound.http.client.HttpClientConfigRegistry;
import com.jay.template.infra.outbound.http.client.resiliency.ResiliencyChainBuilder;
import com.jay.template.infra.outbound.http.client.rest.RestClientFactory;

@Configuration
public class RestClientFactoryConfig {

    @Bean
    public RestClientFactory restClientFactory(
            RestClient.Builder restClientBuilder,
            HttpClientConfigRegistry httpClientConfigRegistry,
            @Qualifier("defaultHttpClientRequestInterceptors")
            List<ClientHttpRequestInterceptor> defaultRequestInterceptors,
            ResiliencyChainBuilder resiliencyChainBuilder
    ) {
        return new RestClientFactory(
                restClientBuilder,
                httpClientConfigRegistry,
                defaultRequestInterceptors,
                resiliencyChainBuilder
        );
    }
}
