package com.jay.template.infra.http.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.jay.template.infra.http.OutboundHttpProperties;

@Component
public class DownstreamPingClient {

    private final RestClient restClient;
    private final String baseUrl;

    public DownstreamPingClient(
            @Qualifier("internalRestClient") RestClient restClient,
            OutboundHttpProperties props
    ) {
        this.restClient = restClient;
        this.baseUrl = props.clients().get("internal").baseUrl();
    }
}
