package com.jay.template.infra.http.client;

import java.net.http.HttpClient;
import java.util.List;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
public class RestClientFactory {

    private final HttpProperties props;
    private final RequestInterceptorRegistry requestInterceptorRegistry;
    private final RestClient.Builder restClientBuilder;

    public RestClientFactory(HttpProperties props,
                             RequestInterceptorRegistry requestInterceptorRegistry,
                             RestClient.Builder restClientBuilder) {
        this.props = props;
        this.requestInterceptorRegistry = requestInterceptorRegistry;
        this.restClientBuilder = restClientBuilder;
    }

    public RestClient buildClient(String clientName) {
        var cfg = props.clients().get(clientName);
        if (cfg == null) {
            throw new IllegalStateException(
                    "Missing outbound http client config: app.outbound.http.clients." + clientName
            );
        }

        var defaults = props.defaults();

        var connectTimeout = cfg.connectTimeoutOrDefault(defaults);
        var readTimeout = cfg.readTimeoutOrDefault(defaults);

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();

        // Bridge JDK HttpClient to Spring RestClient.
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        /* Start from Spring Boot's auto-configured builder to preserve config and micrometer/Otel instrumentation
         * Micrometer / OpenTelemetry client span instrumentation
         * MUST use clone as injected RestClient.Builder is a mutable singleton
         */
        RestClient.Builder builder = restClientBuilder.clone();

        builder.requestFactory(requestFactory)
                .baseUrl(cfg.baseUrl());

        List<ClientHttpRequestInterceptor> requestInterceptors = requestInterceptorRegistry
                .getInterceptors(cfg.requestInterceptorsOrDefault(defaults));
        builder.requestInterceptors(list -> list.addAll(requestInterceptors));


        return builder.build();
    }
}
