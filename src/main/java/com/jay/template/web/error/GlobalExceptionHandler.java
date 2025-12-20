package com.jay.template.web.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jay.template.api.v1.common.error.ErrorResponse;
import com.jay.template.app.error.ApiException;
import com.jay.template.app.error.ErrorType;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ErrorResponseFactory errorResponseFactory;


    public GlobalExceptionHandler(ErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        ErrorType type = ex.getType();

        // NOTE: expected error, no stack trace noise
        // optionally surface errorCode via MDC for identity-complete logging for MDCFilter
        LOGGER.error(type.getCode());

        return errorResponseFactory.buildResponseEntity(type);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        ErrorType type = ErrorType.INTERNAL_SERVER_ERROR;

        LOGGER.error(type.getCode(), ex);

        return errorResponseFactory.buildResponseEntity(type);
    }
}
