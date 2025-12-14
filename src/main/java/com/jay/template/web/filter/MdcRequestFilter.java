package com.jay.template.web.filter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jay.template.infra.logging.MdcProperties;
import com.jay.template.web.request.HttpProperties;

@Component
public class MdcRequestFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MdcRequestFilter.class);

    private final HttpProperties httpProps;
    private final MdcProperties mdcProps;

    public MdcRequestFilter(HttpProperties httpProps, MdcProperties mdcProps) {
        this.httpProps = httpProps;
        this.mdcProps = mdcProps;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long start = System.nanoTime();

        String userId = normalize(request.getHeader(httpProps.headers().userId()));
        String requestId = normalize(request.getHeader(httpProps.headers().requestId()));

        String method = request.getMethod();
        String path = request.getRequestURI();

        try {

            //note that MDC does not allow null values so it removes the key and logback will print empty.

            MDC.put(mdcProps.userId(), userId);
            MDC.put(mdcProps.requestId(), requestId);
            MDC.put(mdcProps.kind(), httpProps.kind());
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
