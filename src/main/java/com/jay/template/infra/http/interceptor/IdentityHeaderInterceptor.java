package com.jay.template.infra.http.interceptor;

import java.io.IOException;

import com.jay.template.infra.http.OutboundHttpProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import com.jay.template.infra.request.Identity;
import com.jay.template.infra.request.IdentityContextHolder;

@Component
public class IdentityHeaderInterceptor implements ClientHttpRequestInterceptor {


    private final OutboundHttpProperties.Headers headerKey;

    public IdentityHeaderInterceptor(OutboundHttpProperties props) {
        this.headerKey = props.headers();
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        HttpHeaders headers = request.getHeaders();
        Identity identity = IdentityContextHolder.getContext().identity();

        headers.set(headerKey.userId(), identity.userId());
        headers.set(headerKey.requestId(), identity.requestId());

        return execution.execute(request, body);
    }
}
