package com.jay.template.web.servlet.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import com.jay.template.api.v1.common.error.ErrorResponse;
import com.jay.template.app.error.ErrorType;
import com.jay.template.web.error.ErrorResponseFactory;

@Component
public class ErrorResponseWriter {

    private final ObjectMapper objectMapper;
    private final ErrorResponseFactory errorResponseFactory;

    public ErrorResponseWriter(
            ObjectMapper objectMapper,
            ErrorResponseFactory errorResponseFactory
    ) {
        this.objectMapper = objectMapper;
        this.errorResponseFactory = errorResponseFactory;
    }

    public void writeJsonErrorResponse(HttpServletResponse response, ErrorType type) throws IOException {

        // defensive safeguard should never happen due to filter-ordering short circuit
        if (response.isCommitted()) {
            return;
        }

        ResponseEntity<ErrorResponse> entity = errorResponseFactory.buildResponseEntity(type);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(entity.getStatusCode().value());

        objectMapper.writeValue(response.getOutputStream(), entity.getBody());
    }
}
