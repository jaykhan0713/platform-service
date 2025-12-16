package com.jay.template.infra.http;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OutboundHttpProperties.class)
public class RestClientConfig {

    private final RestClientFactory factory;

    public RestClientConfig(RestClientFactory factory) {
        this.factory = factory;
    }

    @Bean(name = "internalRestClient")
    public RestClient internalRestClient() {
        return factory.buildClient("internal");
    }

    @Bean(name = "externalRestClient")
    public RestClient externalRestClient() {
        return factory.buildClient("external");
    }
}
