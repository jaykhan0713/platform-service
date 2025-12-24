package com.jay.template.web.servlet.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import com.jay.template.api.v1.common.error.ErrorResponse;
import com.jay.template.app.error.ErrorType;
import com.jay.template.web.error.ErrorResponseSpecFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static com.jay.template.app.error.ErrorType.TOO_MANY_REQUESTS;

class ErrorResponseWriterTest {

    @Test
    void writeErrorResponseWriterTest() throws IOException {
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        ErrorResponseSpecFactory factory = mock(ErrorResponseSpecFactory.class);

        ErrorResponseWriter writer = new ErrorResponseWriter(objectMapper, factory);

        ErrorType type = TOO_MANY_REQUESTS;
        String correlationId = "trace-001";
        ErrorResponse body = new ErrorResponse(type.code(), type.defaultMessage(), correlationId);

        ResponseEntity<ErrorResponse> entity =
                ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);

        when(factory.buildResponseSpec(type)).thenReturn(entity);

        MockHttpServletResponse response = new MockHttpServletResponse();

        writer.writeJsonErrorResponse(response, type);

        verify(factory).buildResponseSpec(type);
        // Verify the writer passes the response output stream and the response body to Jackson
        verify(objectMapper).writeValue(response.getOutputStream(), body);

        MediaType contentType = MediaType.parseMediaType(response.getContentType());
        MediaType expected = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

        assertEquals(expected, contentType);
        assertEquals(entity.getStatusCode().value(), response.getStatus());
    }

    @Test
    void whenResponseIsCommittedReturns() throws IOException {
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        ErrorResponseSpecFactory factory = mock(ErrorResponseSpecFactory.class);

        ErrorResponseWriter writer = new ErrorResponseWriter(objectMapper, factory);

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setCommitted(true);

        writer.writeJsonErrorResponse(response, TOO_MANY_REQUESTS);

        verifyNoInteractions(objectMapper, factory);
    }
}