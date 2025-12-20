package com.jay.template.web.error;

import java.util.stream.Stream;

import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jay.template.api.v1.common.error.ErrorResponse;
import com.jay.template.helper.MockTracerUtils;
import com.jay.template.app.error.ErrorType;


import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

class ErrorResponseFactoryTest {

    private static Stream<Arguments> statusMappings() {
        return Stream.of(
                // 400s
                arguments(ErrorType.BAD_REQUEST, HttpStatus.BAD_REQUEST),
                arguments(ErrorType.USER_ID_MISSING, HttpStatus.BAD_REQUEST),
                arguments(ErrorType.TOO_MANY_REQUESTS, HttpStatus.TOO_MANY_REQUESTS),

                // 500s
                arguments(ErrorType.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR)
        );
    }

    @ParameterizedTest
    @MethodSource("statusMappings")
    void buildResponseEntitySetsStatusAndBody(ErrorType type, HttpStatus expectedStatus) {
        String traceId = "trace-001";
        Tracer tracer = MockTracerUtils.mockTracer(traceId);

        ErrorResponseFactory factory = new ErrorResponseFactory(tracer);

        ResponseEntity<ErrorResponse> response = factory.buildResponseEntity(type);

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

        ErrorResponseFactory factory = new ErrorResponseFactory(tracer);

        ResponseEntity<ErrorResponse> response = factory.buildResponseEntity(ErrorType.INTERNAL_SERVER_ERROR);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().correlationId());
    }

}