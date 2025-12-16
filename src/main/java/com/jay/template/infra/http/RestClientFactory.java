package com.jay.template.infra.http;

import java.net.http.HttpClient;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.jay.template.infra.http.interceptor.IdentityHeaderInterceptor;

@Component
public class RestClientFactory {

    private final OutboundHttpProperties props;
    private final IdentityHeaderInterceptor identityHeaderInterceptor;

    public RestClientFactory(OutboundHttpProperties props,
                             IdentityHeaderInterceptor identityHeaderInterceptor) {
        this.props = props;
        this.identityHeaderInterceptor = identityHeaderInterceptor;
    }

    public RestClient buildClient(String clientName) {
        var cfg = props.clients().get(clientName);
        if (cfg == null) {
            throw new IllegalStateException("Missing outbound http client config: app.outbound.http.clients." + clientName);
        }

        var defaults = props.defaults();

        var connectTimeout = cfg.connectTimeoutOrDefault(defaults);
        var readTimeout = cfg.readTimeoutOrDefault(defaults);

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();

        //bridge JDK HttpClient with Spring RestClient
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        RestClient.Builder builder = RestClient.builder()
                .requestFactory(requestFactory);

        if (Boolean.TRUE.equals(cfg.propagateIdentity())) {
            builder.requestInterceptor(identityHeaderInterceptor);
        }

        return builder.build();
    }
}
