package com.jay.template.web.mvc.error;

import com.jay.template.web.error.ErrorResponseSpecFactory;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jay.template.api.v1.common.error.ErrorResponse;
import com.jay.template.core.error.api.ErrorType;
import com.jay.template.helper.MockTracerUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static com.jay.template.core.error.api.ErrorType.INTERNAL_SERVER_ERROR;

class ErrorResponseSpecFactoryTest {

    @Test
    void buildResponseSpecSetsStatusAndBody() {
        String traceId = "trace-001";
        Tracer tracer = MockTracerUtils.mockTracer(traceId);

        ErrorType type = INTERNAL_SERVER_ERROR;
        ErrorResponseSpecFactory factory = new ErrorResponseSpecFactory(tracer);
        ResponseEntity<ErrorResponse> response = factory.buildResponseSpec(type);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());

        ErrorResponse body = response.getBody();
        assertEquals(type.code(), body.code());
        assertEquals(type.defaultMessage(), body.message());
        assertEquals(traceId, body.correlationId());
    }

    @Test
    void buildResponseSpecSetsNullCorrelationIdWhenNoCurrentSpan() {
        Tracer tracer = mock(Tracer.class);
        when(tracer.currentSpan()).thenReturn(null);

        ErrorType type = INTERNAL_SERVER_ERROR;
        ErrorResponseSpecFactory factory = new ErrorResponseSpecFactory(tracer);
        ResponseEntity<ErrorResponse> response = factory.buildResponseSpec(type);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());

        ErrorResponse body = response.getBody();
        assertEquals(type.code(), body.code());
        assertEquals(type.defaultMessage(), body.message());
        assertNull(body.correlationId());
    }

}