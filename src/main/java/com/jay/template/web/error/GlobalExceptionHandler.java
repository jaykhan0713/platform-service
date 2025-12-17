package com.jay.template.web.error;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jay.template.api.v1.common.error.ErrorResponse;
import com.jay.template.app.error.ApiException;
import com.jay.template.app.error.ErrorType;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        ErrorType type = ex.getType();

        // NOTE: expected error, no stack trace noise
        // TODO: optionally surface errorCode via MDC for identity-complete logging for MDCRequestFilter}
        LOGGER.error(type.getCode());

        return buildResponseEntity(type);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        ErrorType type = ErrorType.INTERNAL_SERVER_ERROR;

        LOGGER.error(type.getCode(), ex);

        return buildResponseEntity(type);
    }

    private ResponseEntity<ErrorResponse> buildResponseEntity(ErrorType type) {

        Span span = tracer.currentSpan();
        String traceId = (span != null) ? span.context().traceId() : null;

        //Don't expose smoke server errors to client, so body uses defaultMessage. Log real error.
        ErrorResponse body = new ErrorResponse(type.getCode(), type.getDefaultMessage(), traceId);

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
