package com.jay.template.infra.outbound.http.client.rest;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Objects;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.jay.template.bootstrap.outbound.http.properties.OutboundHttpProperties;
import com.jay.template.infra.outbound.http.client.resiliency.ResiliencyDecorator;
import com.jay.template.infra.outbound.http.client.interceptor.RequestInterceptorRegistry;

@Component
public class RestClientFactory {

    private final RestClient.Builder restClientBuilder;

    private final RequestInterceptorRegistry requestInterceptorRegistry;
    private final ResiliencyDecorator resiliencyDecorator;
    private final OutboundHttpProperties props;

    public RestClientFactory(
            RestClient.Builder restClientBuilder,
            ResiliencyDecorator resiliencyDecorator,
            RequestInterceptorRegistry requestInterceptorRegistry,
            OutboundHttpProperties props
    ) {
        this.restClientBuilder = restClientBuilder;
        this.resiliencyDecorator = resiliencyDecorator;
        this.requestInterceptorRegistry = requestInterceptorRegistry;
        this.props = props;

    }

    public RestClient buildClient(String clientName) {
        var cfg = props.clients().get(Objects.requireNonNull(clientName));
        if (cfg == null) {
            throw new IllegalStateException(
                    "Missing config: platform.http.clients." + clientName
            );
        }

        var defaults = props.defaults();

        /* Start from Spring Boot's auto-configured builder to preserve config and micrometer/Otel instrumentation
         * Micrometer / OpenTelemetry ping span instrumentation
         * MUST use clone as injected RestClient.Builder is a mutable singleton
         */
        RestClient.Builder builder = restClientBuilder.clone();

        ClientHttpRequestFactory requestFactory = createJdkHttpClientFactory(cfg, defaults);
        requestFactory = resiliencyDecorator.decorate(
                requestFactory,
                cfg.resiliencyOrDefault(defaults),
                defaults.resiliency(),
                clientName
        );

        builder.requestFactory(requestFactory)
                .baseUrl(cfg.baseUrl());

        List<ClientHttpRequestInterceptor> requestInterceptors =
                requestInterceptorRegistry
                        .createInterceptors(cfg.requestInterceptorsOrDefault(defaults));

        builder.requestInterceptors(list -> list.addAll(requestInterceptors));


        return builder.build();
    }

    // signal intent: JDK Http Client usage default, but may want different Http clients in future.
    private ClientHttpRequestFactory createJdkHttpClientFactory(
            OutboundHttpProperties.ClientConfig cfg,
            OutboundHttpProperties.ClientDefaults defaults
    ) {
        var connectTimeout = cfg.connectTimeoutOrDefault(defaults);
        var readTimeout = cfg.readTimeoutOrDefault(defaults);

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
