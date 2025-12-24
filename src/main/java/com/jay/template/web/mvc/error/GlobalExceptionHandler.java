package com.jay.template.web.mvc.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jay.template.api.v1.common.error.ErrorResponse;
import com.jay.template.app.error.ApiException;
import com.jay.template.app.error.ErrorType;
import com.jay.template.web.error.ErrorResponseSpec;
import com.jay.template.web.error.ErrorResponseSpecFactory;

import static com.jay.template.app.error.ErrorType.INTERNAL_SERVER_ERROR;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String MESSAGE_FORMAT = "errorCode={} cause={} {}";

    private final ErrorResponseSpecFactory errorResponseSpecFactory;

    public GlobalExceptionHandler(ErrorResponseSpecFactory errorResponseSpecFactory) {
        this.errorResponseSpecFactory = errorResponseSpecFactory;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        ErrorType type = ex.type();

        // NOTE: expected error, no stack trace noise
        // optionally surface errorCode via MDC for identity-complete logging for MDCFilter

        Throwable throwable = ex.getCause();
        String cause = throwable == null ? "UNKNOWN" : throwable.getClass().getSimpleName();

        LOGGER.error(
                MESSAGE_FORMAT,
                type.code(),
                cause,
                ex.getMessage()
        );

        return buildResponseEntity(type);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        ErrorType type = INTERNAL_SERVER_ERROR;

        LOGGER.error(type.code(), ex);

        return buildResponseEntity(type);
    }

    private ResponseEntity<ErrorResponse> buildResponseEntity(ErrorType type) {
        ErrorResponseSpec spec = errorResponseSpecFactory.buildResponseSpec(type);

        return ResponseEntity
                .status(spec.status())
                .body(spec.body());
    }
}
