package com.jay.template.infra.http;

import java.net.http.HttpClient;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.jay.template.infra.http.interceptor.IdentityHeaderInterceptor;

@Component
public class RestClientFactory {

    private final OutboundHttpProperties props;
    private final IdentityHeaderInterceptor identityHeaderInterceptor;
    private final ObjectProvider<RestClient.Builder> restClientBuilderProvider;
    private final ObservationRegistry observationRegistry;

    public RestClientFactory(OutboundHttpProperties props,
                             IdentityHeaderInterceptor identityHeaderInterceptor,
                             ObjectProvider<RestClient.Builder> restClientBuilderProvider,
                             ObservationRegistry observationRegistry) {
        this.props = props;
        this.identityHeaderInterceptor = identityHeaderInterceptor;
        this.restClientBuilderProvider = restClientBuilderProvider;
        this.observationRegistry = observationRegistry;
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

        // Bridge JDK HttpClient to Spring RestClient
        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        // Start from Spring Boot's auto-configured builder to preserve
        // Micrometer / OpenTelemetry client span instrumentation
        RestClient.Builder builder = restClientBuilderProvider.getObject();

        builder//.requestFactory(requestFactory)
                .observationRegistry(observationRegistry)
                .baseUrl(cfg.baseUrl());

        //header-related appending
        if (Boolean.TRUE.equals(cfg.propagateIdentityOrDefault(defaults))) {
            builder.requestInterceptor(identityHeaderInterceptor);
        }

        String acceptEncoding = cfg.acceptEncodingOrDefault(defaults);
        if (StringUtils.hasText(acceptEncoding)) {
            builder.defaultHeader(HttpHeaders.ACCEPT_ENCODING, acceptEncoding.trim());
        }

        return builder.build();
    }
}
