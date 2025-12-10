package com.jay.template.web.error;

import com.jay.template.error.ApiException;
import com.jay.template.error.ErrorType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String ERROR_FORMAT = "{} exceptionMessage=\"{}\"";

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        ErrorType type = ex.getType();
        ErrorResponse body = ErrorResponse.from(type, type.getDefaultMessage());

        LOGGER.error(type.getCode(), ex);

        return ResponseEntity
                .status(toStatus(type))
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        ErrorType type = ErrorType.INTERNAL_SERVER_ERROR;

        //Don't expose internal server errors to client, so body uses defaultMessage. Log real error.
        ErrorResponse body = ErrorResponse.from(type, type.getDefaultMessage());

        LOGGER.error(type.getCode(), ex);

        return ResponseEntity
                .status(toStatus(type))
                .body(body);
    }

    private HttpStatus toStatus(ErrorType type) {
        return switch (type) {
            case BAD_REQUEST, USER_ID_MISSING -> HttpStatus.BAD_REQUEST;
            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
