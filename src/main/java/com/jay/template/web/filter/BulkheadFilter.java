package com.jay.template.web.filter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.github.resilience4j.bulkhead.Bulkhead;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import com.jay.template.api.v1.common.error.ErrorResponse;
import com.jay.template.app.error.ErrorType;
import com.jay.template.web.error.ErrorResponseFactory;

/**
 * Inbound concurrency guard implemented as a servlet filter.
 *
 * <p>This filter enforces a global filter-level concurrency limit using a
 * Resilience4j {@link io.github.resilience4j.bulkhead.Bulkhead} (semaphore-based).
 * Traditionally Tomcat parsed the request on connector threads and then executed the
 * servlet pipeline (filters onward) on a bounded worker thread pool; with virtual threads
 * that execution is no longer bounded by the worker pool, so we enforce admission
 * control explicitly.</p>
 *
 * <h2>Behavior</h2>
 * <ul>
 *   <li>Runs once per HTTP request (via {@link OncePerRequestFilter}).</li>
 *   <li>Attempts to acquire a bulkhead permit before request processing.</li>
 *   <li>If a permit is available, the request proceeds and the permit is released
 *       when processing completes.</li>
 *   <li>If no permit is available, the request is rejected immediately with
 *       HTTP {@code 429 Too Many Requests}.</li>
 * </ul>
 *
 * <h2>Fail-fast design</h2>
 * <p>This filter should <strong>not</strong> wait for permits. Waiting (parking
 * virtual threads) would retain request state on the heap and lead to latency
 * increase under load. Instead, excess requests are rejected quickly to
 * preserve service stability.</p>
 *
 * <h2>Bypass paths</h2>
 * <p>Health checks and API documentation endpoints are excluded from bulkhead
 * enforcement to ensure observability and operability during overload:</p>
 * <ul>
 *   <li>{@code /actuator/**}</li>
 *   <li>{@code /v3/**}</li>
 *   <li>{@code /swagger*}</li>
 * </ul>
 *
 * <h2>Error handling</h2>
 * <p>Rejections are handled at the filter level (before Spring MVC) and return
 * the standard {@link ErrorResponse} contract using {@link ErrorResponseFactory}.
 * This keeps error responses consistent even when the request never reaches
 * {@code DispatcherServlet}.</p>
 *
 * <h2>Threading model</h2>
 * <p>This filter executes on a virtual thread. The bulkhead limits the number of
 * in-flight requests inside the service, protecting heap, CPU, and downstream
 * dependencies from unbounded concurrency.</p>
 */
class BulkheadFilter extends OncePerRequestFilter {

    private final Bulkhead bulkhead;
    private final ErrorResponseFactory errorResponseFactory;
    private final ObjectMapper objectMapper;

    BulkheadFilter(
            Bulkhead bulkhead,
            ErrorResponseFactory errorResponseFactory,
            ObjectMapper objectMapper
    ) {
        this.bulkhead = bulkhead;
        this.errorResponseFactory = errorResponseFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (bulkhead.tryAcquirePermission()) { //try to acquire semaphore permit
            try {
                filterChain.doFilter(request, response);
            } finally {
                bulkhead.releasePermission();
            }
        } else {
            mapErrorResponse(response);
        }
    }

    private void mapErrorResponse(HttpServletResponse response) throws IOException {
        ResponseEntity<ErrorResponse> entity = errorResponseFactory.buildResponseEntity(ErrorType.TOO_MANY_REQUESTS);

        response.setStatus(entity.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), entity.getBody());
    }
}
