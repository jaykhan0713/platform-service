package com.jay.template.web.error;

import static org.junit.jupiter.api.Assertions.*;

import com.jay.template.error.ErrorType;
import org.junit.jupiter.api.Test;

class ErrorResponseTest {
    @Test
    public void testFromCreatesErrorResponse() {
        ErrorType type = ErrorType.BAD_REQUEST;
        String message = "Bad Request";

        ErrorResponse response = ErrorResponse.from(type, message);

        assertEquals(type.getCode(), response.code());
        assertEquals(message, response.message());
    }
}