package com.jay.template.infra.outbound.http.client.rest;

import java.net.http.HttpClient;
import java.util.List;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.jay.template.core.outbound.http.client.settings.HttpClientSettings;
import com.jay.template.core.port.outbound.http.client.HttpClientSettingsRegistry;
import com.jay.template.infra.outbound.http.client.resiliency.ResiliencyChainAssembler;

public class RestClientFactory {

    private final RestClient.Builder restClientBuilder;
    private final HttpClientSettingsRegistry clientConfigRegistry;
    private final ResiliencyChainAssembler resiliencyChainAssembler;
    private final List<ClientHttpRequestInterceptor> defaultRequestInterceptors;

    public RestClientFactory(
            RestClient.Builder restClientBuilder,
            HttpClientSettingsRegistry clientConfigRegistry,
            List<ClientHttpRequestInterceptor> defaultRequestInterceptors,
            ResiliencyChainAssembler resiliencyChainAssembler
    ) {
        this.restClientBuilder = restClientBuilder;
        this.clientConfigRegistry = clientConfigRegistry;
        this.defaultRequestInterceptors = defaultRequestInterceptors;
        this.resiliencyChainAssembler = resiliencyChainAssembler;
    }

    public RestClient buildClient(String clientName) {
        return buildClient(clientName, defaultRequestInterceptors);
    }

    public RestClient buildClient(
            String clientName,
            List<ClientHttpRequestInterceptor> requestInterceptors
    ) {
        var cfg = clientConfigRegistry.httpClientSettings(clientName);

        /* Start from Spring Boot's auto-configured builder to preserve settings and micrometer/Otel instrumentation
         * Micrometer / OpenTelemetry ping span instrumentation
         * MUST use clone as injected RestClient.Builder is a mutable singleton
         */
        RestClient.Builder builder = restClientBuilder.clone();

        ClientHttpRequestFactory requestFactory = createJdkHttpClientFactory(cfg);
        requestFactory = resiliencyChainAssembler.assemble(
                requestFactory,
                cfg.resiliencyPolicy(),
                cfg.clientName()
        );

        builder.requestFactory(requestFactory)
                .baseUrl(cfg.baseUrl());

        final List<ClientHttpRequestInterceptor> requestInterceptorsOrDefault;

        if (requestInterceptors == null) {
            requestInterceptorsOrDefault = defaultRequestInterceptors;
        } else {
            requestInterceptorsOrDefault = requestInterceptors;
        }

        builder.requestInterceptors(list -> list.addAll(requestInterceptorsOrDefault));

        return builder.build();
    }

    // signal intent: JDK Http Client usage default, but may want different Http clients in future.
    private ClientHttpRequestFactory createJdkHttpClientFactory(
            HttpClientSettings cfg
    ) {
        var connectTimeout = cfg.connectTimeout();
        var readTimeout = cfg.readTimeout();

        /*
         * connectTimeout bounds the entire connection-establishment attempt (DNS + TCP + TLS if https).
         * This is I/O: with virtual threads the caller is typically parked while the OS/network stack
         * completes the connect.
         * JDK HttpClient does not expose a separate “wait for pooled connection” timeout like i.e Apache clients.
         */

        //transport creation, JDK HttpClient
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();

        // Bridge JDK HttpClient to Spring.
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        return requestFactory;
    }
}
