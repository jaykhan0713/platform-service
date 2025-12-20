package com.jay.template.infra.http.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
class RestClientConfig {

    private final RestClientFactory factory;

    public RestClientConfig(RestClientFactory factory) {
        this.factory = factory;
    }

    @Bean(name = "pingRestClient")
    public RestClient pingRestClient() {
        return factory.buildClient("ping");
    }
}
