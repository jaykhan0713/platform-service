package com.jay.template.infra.outbound.http.client.resiliency.circuitbreaker;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;

public final class CircuitBreakerClientHttpRequestFactoryDecorator implements ClientHttpRequestFactory {

    private final ClientHttpRequestFactory delegate;
    private final CircuitBreaker circuitBreaker;

    public CircuitBreakerClientHttpRequestFactoryDecorator(
            ClientHttpRequestFactory delegate,
            CircuitBreaker circuitBreaker
    ) {
        this.delegate = delegate;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        ClientHttpRequest req = delegate.createRequest(uri, httpMethod);
        return new CircuitBreakerClientHttpRequest(req, circuitBreaker);
    }

    private static final class CircuitBreakerClientHttpRequest implements ClientHttpRequest {
        private final ClientHttpRequest delegate;
        private final CircuitBreaker circuitBreaker;

        private CircuitBreakerClientHttpRequest(ClientHttpRequest delegate, CircuitBreaker circuitBreaker) {
            this.delegate = delegate;
            this.circuitBreaker = circuitBreaker;
        }

        @Override
        public ClientHttpResponse execute() throws IOException {
            try {
                return circuitBreaker.executeCallable(delegate::execute);
            } catch (IOException | CallNotPermittedException ex) {
                throw ex;
            } catch (Exception ex) { //needs wider Exception for executeCallable
                throw new IOException(ex);
            }
        }

        @Override public OutputStream getBody() throws IOException { return delegate.getBody(); }

        @Override public HttpMethod getMethod() { return delegate.getMethod(); }

        @Override public URI getURI() { return delegate.getURI(); }

        @Override public Map<String, Object> getAttributes() { return delegate.getAttributes(); }

        @Override public HttpHeaders getHeaders() { return delegate.getHeaders(); }
    }
}
