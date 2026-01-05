package com.jay.template.web.servlet.error;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import com.jay.template.core.error.api.ErrorType;
import com.jay.template.web.error.ErrorResponseSpec;
import com.jay.template.web.error.ErrorResponseSpecFactory;

// for servlet/ mapping
@Component
public class ErrorResponseWriter {

    private final ObjectMapper objectMapper;
    private final ErrorResponseSpecFactory errorResponseSpecFactory;

    public ErrorResponseWriter(
            ObjectMapper objectMapper,
            ErrorResponseSpecFactory errorResponseSpecFactory
    ) {
        this.objectMapper = objectMapper;
        this.errorResponseSpecFactory = errorResponseSpecFactory;
    }

    public void writeJsonErrorResponse(HttpServletResponse response, ErrorType type) throws IOException {

        // defensive safeguard should never happen due to filter-ordering short circuit
        if (response.isCommitted()) {
            return;
        }

        ErrorResponseSpec spec = errorResponseSpecFactory.buildResponseSpec(type);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(spec.status().value());

        objectMapper.writeValue(response.getOutputStream(), spec.body());
    }
}
