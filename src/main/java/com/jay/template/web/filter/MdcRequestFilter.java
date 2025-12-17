package com.jay.template.web.filter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.jay.template.infra.identity.IdentityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jay.template.infra.logging.MdcProperties;

/**
 * Populates MDC (Mapped Diagnostic Context) fields for structured identity logging.
 *
 * <p>
 * {@code MdcRequestFilter} extracts a small set of identity attributes and configured
 * inbound headers and writes them into {@link MDC} so all log entries produced during
 * identity handling include consistent context.
 * </p>
 *
 * <p>
 * This filter does not depend on {@code IdentityContextHolder}. It reads headers
 * directly and normalizes missing values to empty strings to ensure MDC keys are
 * always present in log output.
 * </p>
 *
 * <p>
 * MDC is cleared in a {@code finally} block to prevent context leakage across thread
 * reuse.
 * </p>
 */
@Component
public class MdcRequestFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MdcRequestFilter.class);

    private final IdentityProperties.Http.Headers headerKeys;
    private final MdcProperties mdcProps;

    public MdcRequestFilter(IdentityProperties identityProps, MdcProperties mdcProps) {
        this.headerKeys = identityProps.http().headers();
        this.mdcProps = mdcProps;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long start = System.nanoTime();

        String userId = normalize(request.getHeader(headerKeys.userId()));
        String requestId = normalize(request.getHeader(headerKeys.requestId()));
        // additional headers can be added here

        String method = request.getMethod();
        String path = request.getRequestURI();

        try {

            //note that MDC does not allow null values so it removes the key and logback will print empty.

            MDC.put(mdcProps.userId(), userId);
            MDC.put(mdcProps.requestId(), requestId);
            MDC.put(mdcProps.kind(), mdcProps.kindValues().http());
            MDC.put(mdcProps.method(), method);
            MDC.put(mdcProps.name(), path);

            filterChain.doFilter(request, response);
        } finally {

            MDC.put(mdcProps.status(), String.valueOf(response.getStatus()));

            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            MDC.put(mdcProps.durationMs(), String.valueOf(durationMs));

            LOGGER.info("");
            MDC.clear();
        }
    }

    // normalizing to empty string ensures key is always present, even with missing value
    private String normalize(String value) {
        return value == null ? "" : value;
    }
}
