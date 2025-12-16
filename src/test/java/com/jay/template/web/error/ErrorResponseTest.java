package com.jay.template.web.error;

import org.junit.jupiter.api.Test;

import com.jay.template.api.v1.common.error.ErrorResponse;
import com.jay.template.error.ErrorType;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void fromCreatesErrorResponse() {
        ErrorType type = ErrorType.BAD_REQUEST;
        String correlationId = "trace-001";

        ErrorResponse response = new ErrorResponse(type.getCode(), type.getDefaultMessage(), correlationId);

        assertEquals(type.getCode(), response.code());
        assertEquals(type.getDefaultMessage(), response.message());
        assertEquals(correlationId, response.correlationId());
    }
}