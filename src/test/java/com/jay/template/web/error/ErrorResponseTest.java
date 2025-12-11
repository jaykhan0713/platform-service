package com.jay.template.web.error;

import static org.junit.jupiter.api.Assertions.*;

import com.jay.template.error.ErrorType;
import org.junit.jupiter.api.Test;

class ErrorResponseTest {
    @Test
    public void testFromCreatesErrorResponse() {
        ErrorType type = ErrorType.BAD_REQUEST;
        String correlationId = "trace-001";

        ErrorResponse response = ErrorResponse.from(type, correlationId);

        assertEquals(type.getCode(), response.code());
        assertEquals(type.getDefaultMessage(), response.message());
        assertEquals(correlationId, response.correlationId());
    }
}