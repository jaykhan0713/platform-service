package com.jay.template.app.error;

import com.jay.template.app.error.ApiException;
import com.jay.template.app.error.ErrorType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionTest {

    @Test
    void typeIsSet() {
        ApiException exception = new ApiException(ErrorType.BAD_REQUEST);
        assertEquals(ErrorType.BAD_REQUEST, exception.type());
    }

    @Test
    void typeConstructorUsesDefaultMessage() {
        ApiException ex = new ApiException(ErrorType.BAD_REQUEST);
        assertEquals(ErrorType.BAD_REQUEST.defaultMessage(), ex.getMessage());
    }

    @Test
    void customMessageIsUsed() {
        ApiException ex = new ApiException(ErrorType.BAD_REQUEST, "msg=\"custom message\"");
        assertEquals("msg=\"custom message\"", ex.getMessage());
    }
}