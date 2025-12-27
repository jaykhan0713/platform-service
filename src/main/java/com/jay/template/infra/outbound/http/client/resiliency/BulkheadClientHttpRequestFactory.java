package com.jay.template.infra.outbound.http.client.resiliency;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
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
                AtomicBoolean permitHeld = new AtomicBoolean(true);

                // 1 permit held needs exactly 1 safe release. A release-once gate.
                Runnable releaseOnceGate = () -> {
                    if (permitHeld.compareAndSet(true, false)) {
                        bulkhead.releasePermission();
                    }
                };

                try {
                    ClientHttpResponse response = delegate.execute();
                    return new BulkheadClientHttpResponse(response, releaseOnceGate);
                } catch (RuntimeException | IOException ex) {
                    releaseOnceGate.run();
                    throw ex;
                }
                /* NOTE: Do not want to finally release, as we want the permit only to be released
                 * when response body is fully consumed by higher layers and close() is called.
                 * ClientHttpResponse.close() for buffered responses and InputStream close()
                 * for streamed responses.
                 */

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

    private static final class BulkheadClientHttpResponse implements ClientHttpResponse {

        private final ClientHttpResponse delegate;
        private final Runnable releaseOnceGate;

        /*
         * Cached body avoids multiple delegate.getBody() calls.
         * Response bodies are consumed single-threaded.
         */
        private InputStream cachedBody;

        private BulkheadClientHttpResponse(
                ClientHttpResponse delegate,
                Runnable releaseOnceGate
        ) {
            this.delegate = delegate;
            this.releaseOnceGate = releaseOnceGate;
        }

        /*
         * Spring consumption: Spring reads the response body fully (via converters),
         * maps to a DTO, and then closes the response. Releasing on close() holds the
         * permit for the full in-flight lifetime.
         *
         * Note: Some implementations close the body stream as part of response.close(),
         * and some close the body stream explicitly after consumption. We release on both
         * response close and body close (guarded) so either lifecycle signal can end the permit.
         *
         * Ultimately, both signals represent the end of HTTP IO consumption, which is where
         * this bulkhead permit should be released.
         */
        @Override
        public void close() {
            try {
                delegate.close();
            } finally {
                releaseOnceGate.run();
            }
        }

        /*
         * Streamed consumption: higher layers read the InputStream directly.
         * The permit must be held until the caller closes the stream.
         *
         * socket -> InputStream -> consumer code -> (read until EOF) -> inputStream.close()
         *
         */
        @Override public InputStream getBody() throws IOException {
            if (cachedBody == null) {
                cachedBody = new FilterInputStream(delegate.getBody()) {
                    @Override
                    public void close() throws IOException {
                        try {
                            super.close();
                        } finally {
                            releaseOnceGate.run();
                        }
                    }
                };
            }

            return cachedBody;
        }

        @Override public HttpStatusCode getStatusCode() throws IOException { return delegate.getStatusCode(); }

        @Override public String getStatusText() throws IOException { return delegate.getStatusText(); }

        @Override public HttpHeaders getHeaders() { return delegate.getHeaders(); }
    }
}
