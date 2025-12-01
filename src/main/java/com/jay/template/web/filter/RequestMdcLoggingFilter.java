package com.jay.template.web.filter;

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

import java.io.IOException;

@Component
public class RequestMdcLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaDataLogger.class);

    private final MdcProperties props;

    public RequestMdcLoggingFilter(MdcProperties props) {
        this.props = props;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long start = System.currentTimeMillis();
        String method = request.getMethod();
        String path = request.getRequestURI();

        try {

            this.props.getHeaders().forEach((headerName, mdcKey) -> {
                String value = request.getHeader(headerName);
                if (value != null && !value.isBlank()) {
                    MDC.put(mdcKey, value);
                }
            });

            filterChain.doFilter(request, response);
        } finally {
            MDC.put(props.getMethod(), method);
            MDC.put(props.getPath(), path);
            MDC.put(props.getStatus(), String.valueOf(response.getStatus()));
            MDC.put(props.getDurationMs(), String.valueOf(System.currentTimeMillis() - start));

            LOGGER.info("request_complete");
            MDC.clear();
        }
    }
}
