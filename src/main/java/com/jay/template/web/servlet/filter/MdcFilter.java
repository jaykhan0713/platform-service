package com.jay.template.web.servlet.filter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.jay.template.core.context.identity.Identity;
import com.jay.template.core.context.identity.IdentityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jay.template.bootstrap.observability.properties.MdcProperties;

/**
 * Populates MDC (Mapped Diagnostic Context) fields for structured identity logging.
 *
 * <p>
 * {@code MdcFilter} extracts a small set of identity attributes and configured
 * inbound headers and writes them into {@link MDC} so all log entries produced during
 * identity handling include consistent propagation.
 * </p>
 *
 * <p>
 * This filter does not depend on {@code IdentityContextHolder}. It reads headers
 * directly and normalizes missing values to empty strings to ensure MDC keys are
 * always present in log output.
 * </p>
 *
 * <p>
 * MDC is cleared in a {@code finally} block to prevent propagation leakage across thread
 * reuse.
 * </p>
 */
public class MdcFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MdcFilter.class);

    private final MdcProperties mdcProps;

    MdcFilter(MdcProperties mdcProps) {
        this.mdcProps = mdcProps;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long start = System.nanoTime();

        Identity identity = IdentityContextHolder.context().identity();
        String userId = identity.userId();
        String requestId = identity.requestId();

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
}
