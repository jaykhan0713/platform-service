package com.jay.template.error;

import com.jay.template.infra.error.ApiException;
import com.jay.template.infra.error.ErrorType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionTest {

    @Test
    void typeIsSet() {
        ApiException exception = new ApiException(ErrorType.BAD_REQUEST);
        assertEquals(ErrorType.BAD_REQUEST, exception.getType());
    }

    @Test
    void typeConstructorUsesDefaultMessage() {
        ApiException ex = new ApiException(ErrorType.BAD_REQUEST);
        assertEquals(ErrorType.BAD_REQUEST.getDefaultMessage(), ex.getMessage());
    }

    @Test
    void customMessageIsUsed() {
        ApiException ex = new ApiException(ErrorType.BAD_REQUEST, "custom message");
        assertEquals("custom message", ex.getMessage());
    }
}