package com.jay.template.infra.outbound.http.client.interceptor;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.jay.template.core.context.identity.Identity;
import com.jay.template.core.context.identity.IdentityContextHolder;
import com.jay.template.core.transport.http.IdentityHeaders;

public class IdentityHeaderInterceptor implements ClientHttpRequestInterceptor {

    private final IdentityHeaders headers;

    public IdentityHeaderInterceptor(IdentityHeaders headers) {
        this.headers = headers;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        HttpHeaders headers = request.getHeaders();
        Identity identity = IdentityContextHolder.context().identity();

        headers.set(this.headers.userId(), identity.userId());
        headers.set(this.headers.requestId(), identity.requestId());

        return execution.execute(request, body);
    }
}
