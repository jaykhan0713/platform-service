package com.jay.template.infra.outbound.http.client.rest;

import java.net.http.HttpClient;
import java.util.List;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.jay.template.core.outbound.http.client.config.HttpClientConfig;
import com.jay.template.core.port.outbound.http.client.HttpClientConfigRegistry;
import com.jay.template.infra.outbound.http.client.resiliency.ResiliencyChainBuilder;

public class RestClientFactory {

    private final RestClient.Builder restClientBuilder;
    private final HttpClientConfigRegistry clientConfigRegistry;
    private final ResiliencyChainBuilder resiliencyChainBuilder;
    private final List<ClientHttpRequestInterceptor> defaultRequestInterceptors;

    public RestClientFactory(
            RestClient.Builder restClientBuilder,
            HttpClientConfigRegistry clientConfigRegistry,
            List<ClientHttpRequestInterceptor> defaultRequestInterceptors,
            ResiliencyChainBuilder resiliencyChainBuilder
    ) {
        this.restClientBuilder = restClientBuilder;
        this.clientConfigRegistry = clientConfigRegistry;
        this.defaultRequestInterceptors = defaultRequestInterceptors;
        this.resiliencyChainBuilder = resiliencyChainBuilder;
    }

    public RestClient buildClient(String clientName) {
        return buildClient(clientName, defaultRequestInterceptors);
    }

    public RestClient buildClient(
            String clientName,
            List<ClientHttpRequestInterceptor> requestInterceptors
    ) {
        var cfg = clientConfigRegistry.httpClientConfig(clientName);

        /* Start from Spring Boot's auto-configured builder to preserve config and micrometer/Otel instrumentation
         * Micrometer / OpenTelemetry ping span instrumentation
         * MUST use clone as injected RestClient.Builder is a mutable singleton
         */
        RestClient.Builder builder = restClientBuilder.clone();

        ClientHttpRequestFactory requestFactory = createJdkHttpClientFactory(cfg);
        requestFactory = resiliencyChainBuilder.applyBulkhead(
                requestFactory,
                cfg.resiliencyConfig(),
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
            HttpClientConfig cfg
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
