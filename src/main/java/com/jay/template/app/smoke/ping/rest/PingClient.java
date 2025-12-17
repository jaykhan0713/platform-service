package com.jay.template.app.smoke.ping.rest;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.jay.template.app.smoke.ping.contract.outbound.DownstreamPingResponse;

@Component
public class PingClient {

    private final RestClient restClient;

    public PingClient(@Qualifier("pingRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public DownstreamPingResponse ping() {
        return restClient.get()
                .uri("/outbound/ping")
                .retrieve()
                .body(DownstreamPingResponse.class);
    }
}
