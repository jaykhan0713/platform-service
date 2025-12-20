package com.jay.template.web.error;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.jay.template.api.v1.common.error.ErrorResponse;
import com.jay.template.app.error.ErrorType;

@Component
public class ErrorResponseFactory {

    private final Tracer tracer;

    public ErrorResponseFactory(Tracer tracer) {
        this.tracer = tracer;
    }

    public ResponseEntity<ErrorResponse> buildResponseEntity(ErrorType type) {

        Span span = tracer.currentSpan();
        String traceId = (span != null) ? span.context().traceId() : null;

        //Don't expose server errors to client, so body uses defaultMessage. Log real error.
        ErrorResponse body = new ErrorResponse(type.getCode(), type.getDefaultMessage(), traceId);

        return ResponseEntity
                .status(toStatus(type))
                .body(body);
    }

    private HttpStatus toStatus(ErrorType type) {
        return switch (type) {
            //400
            case BAD_REQUEST, USER_ID_MISSING -> HttpStatus.BAD_REQUEST;
            case TOO_MANY_REQUESTS -> HttpStatus.TOO_MANY_REQUESTS;

            //500
            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
