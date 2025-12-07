package com.jay.template.web.error;

import com.jay.template.logging.mdc.MdcRetriever;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MdcRetriever mdcRetriever;

    public GlobalExceptionHandler(MdcRetriever mdcRetriever) {
        this.mdcRetriever = mdcRetriever;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        ErrorType type = ex.getType();
        ErrorResponse body = ErrorResponse.from(type, ex.getMessage(), mdcRetriever.getGatewayTraceId());

        LOGGER.error(type.getCode(), ex);

        return ResponseEntity
                .status(type.getStatus())
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        ErrorType type = ErrorType.INTERNAL_SERVER_ERROR;

        //Don't expose internal server errors to client, so body uses defaultMessage. Log real error.
        ErrorResponse body = ErrorResponse.from(type, type.getDefaultMessage(), mdcRetriever.getGatewayTraceId());

        LOGGER.error(type.getCode(), ex);

        return ResponseEntity
                .status(type.getStatus())
                .body(body);
    }
}
