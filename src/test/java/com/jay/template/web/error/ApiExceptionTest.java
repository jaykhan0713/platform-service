package com.jay.template.web.error;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ApiExceptionTest {

    @Test
    public void testTypeIsSet() {
        ApiException exception = new ApiException(ErrorType.BAD_REQUEST);
        assertEquals(ErrorType.BAD_REQUEST, exception.getType());
    }

    @Test
    public void typeConstructorUsesDefaultMessage() {
        ApiException ex = new ApiException(ErrorType.BAD_REQUEST);
        assertEquals(ErrorType.BAD_REQUEST.getDefaultMessage(), ex.getMessage());
    }

    @Test
    public void customMessageIsUsed() {
        ApiException ex = new ApiException(ErrorType.BAD_REQUEST, "custom message");
        assertEquals("custom message", ex.getMessage());
    }
}