package com.jay.template.infra.outbound.http.client.interceptor;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import com.jay.template.infra.identity.Identity;
import com.jay.template.infra.identity.IdentityContextHolder;
import com.jay.template.infra.identity.IdentityProperties;

@Component
public class IdentityHeaderInterceptor implements ClientHttpRequestInterceptor, RequestInterceptorFeature {

    private static final String KEY = "identity";

    private final IdentityProperties.Http.Headers headerKeys;

    public IdentityHeaderInterceptor(IdentityProperties props) {
        this.headerKeys = props.http().headers();
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        HttpHeaders headers = request.getHeaders();
        Identity identity = IdentityContextHolder.context().identity();

        headers.set(headerKeys.userId(), identity.userId());
        headers.set(headerKeys.requestId(), identity.requestId());

        return execution.execute(request, body);
    }

    @Override
    public ClientHttpRequestInterceptor interceptor() {
        return this;
    }

    @Override
    public String key() {
        return KEY;
    }
}
