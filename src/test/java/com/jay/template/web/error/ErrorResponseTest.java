package com.jay.template.web.error;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ErrorResponseTest {
    @Test
    public void testFromCreatesErrorResponse() {
        ErrorType type = ErrorType.BAD_REQUEST;
        String message = "Bad Request";
        String gatewayTraceId = "trace-123";

        ErrorResponse response = ErrorResponse.from(type, message, gatewayTraceId);

        assertEquals(type.getCode(), response.code());
        assertEquals(message, response.message());
        assertEquals(gatewayTraceId, response.gatewayTraceId());
    }
}