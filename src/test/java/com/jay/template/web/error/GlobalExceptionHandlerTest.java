package com.jay.template.web.error;

import com.jay.template.error.ApiException;
import com.jay.template.error.ErrorType;
import com.jay.template.helper.MockTracerUtils;

import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @Test
    void handleGenericException() {
        String message = "generic error";
        Exception ex = new Exception(message);
        String traceId = "trace-001";
        Tracer tracer = MockTracerUtils.mockTracer(traceId);

        GlobalExceptionHandler handler = new GlobalExceptionHandler(tracer);
        ResponseEntity<ErrorResponse> entity = handler.handleGenericException(ex);

        ErrorResponse body = entity.getBody();
        assertNotNull(body);
        assertEquals(ErrorType.INTERNAL_SERVER_ERROR.getDefaultMessage(), body.message());
        assertEquals(ErrorType.INTERNAL_SERVER_ERROR.getCode(), body.code());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());
        assertEquals(traceId, body.correlationId());
    }

    @Test
    void handleApiException() {
        String message = "bad request error";
        ApiException ex = new ApiException(ErrorType.BAD_REQUEST, message);
        String traceId = "trace-001";
        Tracer tracer = MockTracerUtils.mockTracer(traceId);

        GlobalExceptionHandler handler = new GlobalExceptionHandler(tracer);
        ResponseEntity<ErrorResponse> entity = handler.handleApiException(ex);

        ErrorResponse body = entity.getBody();
        assertNotNull(body);
        assertEquals(ErrorType.BAD_REQUEST.getDefaultMessage(), body.message());
        assertEquals(ErrorType.BAD_REQUEST.getCode(), body.code());
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
        assertEquals(traceId, body.correlationId());
    }

    @Test
    void handleExceptionWithNullSpan() {
        String message = "generic error";
        Exception ex = new Exception(message);
        String traceId = "trace-001";
        Tracer tracer = Mockito.mock(Tracer.class);

        GlobalExceptionHandler handler = new GlobalExceptionHandler(tracer);
        ResponseEntity<ErrorResponse> entity = handler.handleGenericException(ex);

        ErrorResponse body = entity.getBody();
        assertNotNull(body);
        assertEquals(ErrorType.INTERNAL_SERVER_ERROR.getDefaultMessage(), body.message());
        assertEquals(ErrorType.INTERNAL_SERVER_ERROR.getCode(), body.code());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());
        assertNull(body.correlationId());
    }
}