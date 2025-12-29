package com.jay.template.bootstrap.outbound.http.client.rest.config;

import com.jay.template.infra.outbound.http.client.rest.ping.DownstreamPingResponseMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jay.template.core.port.dependency.ping.PingDependency;
import com.jay.template.infra.outbound.http.client.rest.RestClientFactory;
import com.jay.template.infra.outbound.http.client.rest.ping.PingRestClientAdapter;

@Configuration
public class PingRestClientConfig {

    @Bean
    public PingDependency pingDependency(RestClientFactory restClientFactory) {
        return new PingRestClientAdapter(
                restClientFactory.buildClient("ping"),
                "/api/v1/ping",
                new DownstreamPingResponseMapper()
        );
    }
}
