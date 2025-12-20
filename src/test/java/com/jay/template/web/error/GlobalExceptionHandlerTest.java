package com.jay.template.web.error;

import org.junit.jupiter.api.Test;

import com.jay.template.app.error.ApiException;
import com.jay.template.app.error.ErrorType;

import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    @Test
    void handlesGenericException() {
        Exception ex = mock(Exception.class);

        ErrorResponseFactory errorResponseFactory = mock(ErrorResponseFactory.class);

        GlobalExceptionHandler handler = new GlobalExceptionHandler(errorResponseFactory);
        handler.handleGenericException(ex);

        verify(errorResponseFactory).buildResponseEntity(ErrorType.INTERNAL_SERVER_ERROR);
    }

    @Test
    void handlesApiException() {
        ErrorType type = ErrorType.USER_ID_MISSING;
        ApiException ex = new ApiException(type);

        ErrorResponseFactory errorResponseFactory = mock(ErrorResponseFactory.class);

        GlobalExceptionHandler handler = new GlobalExceptionHandler(errorResponseFactory);
        handler.handleApiException(ex);

        verify(errorResponseFactory).buildResponseEntity(type);
    }
}