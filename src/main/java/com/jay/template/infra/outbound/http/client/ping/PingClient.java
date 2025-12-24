package com.jay.template.infra.outbound.http.client.ping;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.jay.template.app.smoke.outbound.ping.contract.DownstreamPingResponse;
import com.jay.template.infra.outbound.http.client.rest.RestClientFactory;

@Component
public class PingClient {
    private static final String CLIENT_NAME = "ping"; // yaml binded name.

    private final RestClient restClient;

    public PingClient(RestClientFactory factory) {
        this.restClient = factory.buildClient(CLIENT_NAME);
    }

    public DownstreamPingResponse ping() {
        return restClient.get()
                .uri("/api/v1/ping")
                .retrieve()
                .body(DownstreamPingResponse.class);
        /*TODO: handle when response has 4xx/5xx via onStatus()
         *
         * currently no error DTO for this path, so spring just throws RestClientResponseException
         * which is surfaced up to a GlobalExceptionHandler as a generic exception.
         */
    }
}
