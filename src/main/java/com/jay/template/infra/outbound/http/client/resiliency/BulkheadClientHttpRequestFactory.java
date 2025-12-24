package com.jay.template.infra.outbound.http.client.resiliency;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;

final class BulkheadClientHttpRequestFactory implements ClientHttpRequestFactory {

    private final ClientHttpRequestFactory delegate;
    private final Bulkhead bulkhead;

    BulkheadClientHttpRequestFactory(ClientHttpRequestFactory delegate, Bulkhead bulkhead) {
        this.delegate = delegate;
        this.bulkhead = bulkhead;
    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        ClientHttpRequest req = delegate.createRequest(uri, httpMethod);
        return new BulkheadClientHttpRequest(req, bulkhead);
    }

    private static final class BulkheadClientHttpRequest implements ClientHttpRequest {
        private final ClientHttpRequest delegate;
        private final Bulkhead bulkhead;

        private BulkheadClientHttpRequest(ClientHttpRequest delegate, Bulkhead bulkhead) {
            this.delegate = delegate;
            this.bulkhead = bulkhead;
        }

        @Override
        public ClientHttpResponse execute() throws IOException {

            if (bulkhead.tryAcquirePermission()) {
                try {
                    return delegate.execute();
                } finally {
                    bulkhead.releasePermission();
                }
            } else {
                throw BulkheadFullException.createBulkheadFullException(bulkhead);
            }
        }

        @Override public OutputStream getBody() throws IOException { return delegate.getBody(); }

        @Override public HttpMethod getMethod() { return delegate.getMethod(); }

        @Override public URI getURI() { return delegate.getURI(); }

        @Override public Map<String, Object> getAttributes() { return delegate.getAttributes(); }

        @Override public HttpHeaders getHeaders() { return delegate.getHeaders(); }
    }
}
