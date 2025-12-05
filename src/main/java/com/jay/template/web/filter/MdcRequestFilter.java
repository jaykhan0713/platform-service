package com.jay.template.web.filter;

import java.io.IOException;

import com.jay.template.logging.logger.MetaDataLogger;
import com.jay.template.logging.properties.MdcProperties;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class MdcRequestFilter extends OncePerRequestFilter {

    private static final Logger META_DATA_LOGGER = LoggerFactory.getLogger(MetaDataLogger.class);

    private final MdcProperties props;

    public MdcRequestFilter(MdcProperties props) {
        this.props = props;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long start = System.currentTimeMillis();
        String method = request.getMethod();
        String path = request.getRequestURI();
        request.getRequestId();

        try {

            props.getHeaders().forEach((headerName, mdcKey) -> {
                String value = request.getHeader(headerName);
                if (value != null && !value.isBlank()) {
                    MDC.put(mdcKey, value);
                }
            });

            filterChain.doFilter(request, response);
        } finally {

            if (props.getMethod() != null && !props.getMethod().isBlank()) {
                MDC.put(props.getMethod(), method);
            }

            if (props.getPath() != null && !props.getPath().isBlank()) {
                MDC.put(props.getPath(), path);
            }

            if (props.getStatus() != null && !props.getStatus().isBlank()) {
                MDC.put(props.getStatus(), String.valueOf(response.getStatus()));
            }

            if (props.getDurationMs() != null && !props.getDurationMs().isBlank()) {
                MDC.put(props.getDurationMs(), String.valueOf(System.currentTimeMillis() - start));
            }

            META_DATA_LOGGER.info("request_complete");
            MDC.clear();
        }
    }
}
