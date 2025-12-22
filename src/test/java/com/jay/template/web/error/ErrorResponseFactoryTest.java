package com.jay.template.web.error;

import com.jay.template.api.v1.common.error.ErrorResponse;
import com.jay.template.app.error.ErrorType;
import com.jay.template.helper.MockTracerUtils;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static com.jay.template.app.error.ErrorType.INTERNAL_SERVER_ERROR;

class ErrorResponseFactoryTest {

    @Test
    void buildResponseEntitySetsStatusAndBody() {
        String traceId = "trace-001";
        Tracer tracer = MockTracerUtils.mockTracer(traceId);

        ErrorType type = ErrorType.INTERNAL_SERVER_ERROR;
        HttpStatus expectedStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorTypeHttpStatusMapper statusMapper = mock(ErrorTypeHttpStatusMapper.class);
        when(statusMapper.mapErrorTypeToHttpStatus(type))
                .thenReturn(expectedStatus);

        ErrorResponseFactory factory = new ErrorResponseFactory(tracer, statusMapper);

        ResponseEntity<ErrorResponse> response = factory.buildResponseEntity(type);

        verify(statusMapper).mapErrorTypeToHttpStatus(type);

        assertEquals(expectedStatus, response.getStatusCode());
        assertNotNull(response.getBody());

        ErrorResponse body = response.getBody();
        assertEquals(type.getCode(), body.code());
        assertEquals(type.getDefaultMessage(), body.message());
        assertEquals(traceId, body.correlationId());
    }

    @Test
    void buildResponseEntitySetsNullCorrelationIdWhenNoCurrentSpan() {
        Tracer tracer = mock(Tracer.class);
        when(tracer.currentSpan()).thenReturn(null);

        ErrorType type = INTERNAL_SERVER_ERROR;
        HttpStatus expectedStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorTypeHttpStatusMapper statusMapper = mock(ErrorTypeHttpStatusMapper.class);
        when(statusMapper.mapErrorTypeToHttpStatus(type))
                .thenReturn(expectedStatus);

        ErrorResponseFactory factory = new ErrorResponseFactory(tracer, statusMapper);

        ResponseEntity<ErrorResponse> response = factory.buildResponseEntity(ErrorType.INTERNAL_SERVER_ERROR);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());

        ErrorResponse body = response.getBody();
        assertEquals(type.getCode(), body.code());
        assertEquals(type.getDefaultMessage(), body.message());
        assertNull(body.correlationId());
    }

}