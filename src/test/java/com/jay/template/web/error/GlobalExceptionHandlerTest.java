package com.jay.template.web.error;

import static org.junit.jupiter.api.Assertions.*;

import com.jay.template.error.ApiException;
import com.jay.template.error.ErrorType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    @Test
    void testHandleGenericException() {
        String message = "generic error";
        Exception ex = new Exception(message);

        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<ErrorResponse> entity = handler.handleGenericException(ex);

        ErrorResponse body = entity.getBody();
        assertNotNull(body);
        assertEquals(ErrorType.INTERNAL_SERVER_ERROR.getDefaultMessage(), body.message());
        assertEquals(ErrorType.INTERNAL_SERVER_ERROR.getCode(), body.code());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());
    }

    @Test
    public void testHandleApiException() {
        String message = "bad request error";
        ApiException ex = new ApiException(ErrorType.BAD_REQUEST, message);

        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<ErrorResponse> entity = handler.handleApiException(ex);

        ErrorResponse body = entity.getBody();
        assertNotNull(body);
        assertEquals(ErrorType.BAD_REQUEST.getDefaultMessage(), body.message());
        assertEquals(ErrorType.BAD_REQUEST.getCode(), body.code());
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    }
}