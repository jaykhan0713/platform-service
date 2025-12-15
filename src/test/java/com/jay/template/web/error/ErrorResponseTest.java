package com.jay.template.web.error;

import org.junit.jupiter.api.Test;

import com.jay.template.infra.error.ErrorType;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void fromCreatesErrorResponse() {
        ErrorType type = ErrorType.BAD_REQUEST;
        String correlationId = "trace-001";

        ErrorResponse response = ErrorResponse.from(type, correlationId);

        assertEquals(type.getCode(), response.code());
        assertEquals(type.getDefaultMessage(), response.message());
        assertEquals(correlationId, response.correlationId());
    }
}