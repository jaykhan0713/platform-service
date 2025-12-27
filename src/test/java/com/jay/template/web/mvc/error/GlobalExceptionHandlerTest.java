package com.jay.template.web.mvc.error;

import com.jay.template.web.error.ErrorResponseSpecFactory;
import org.junit.jupiter.api.Test;

import com.jay.template.core.error.api.ApiException;
import com.jay.template.core.error.api.ErrorType;

import static org.mockito.Mockito.*;

import static com.jay.template.core.error.api.ErrorType.INTERNAL_SERVER_ERROR;
import static com.jay.template.core.error.api.ErrorType.USER_ID_MISSING;

class GlobalExceptionHandlerTest {

    @Test
    void handlesGenericException() {
        Exception ex = mock(Exception.class);

        ErrorResponseSpecFactory errorResponseSpecFactory = mock(ErrorResponseSpecFactory.class);

        GlobalExceptionHandler handler = new GlobalExceptionHandler(errorResponseSpecFactory);
        handler.handleGenericException(ex);

        verify(errorResponseSpecFactory).buildResponseSpec(INTERNAL_SERVER_ERROR);
    }

    @Test
    void handlesApiException() {
        ErrorType type = USER_ID_MISSING;
        ApiException ex = new ApiException(type);

        ErrorResponseSpecFactory errorResponseSpecFactory = mock(ErrorResponseSpecFactory.class);

        GlobalExceptionHandler handler = new GlobalExceptionHandler(errorResponseSpecFactory);
        handler.handleApiException(ex);

        verify(errorResponseSpecFactory).buildResponseSpec(type);
    }
}