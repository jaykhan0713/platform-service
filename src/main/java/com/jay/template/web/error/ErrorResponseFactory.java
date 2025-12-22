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
    private final ErrorTypeHttpStatusMapper statusMapper;

    public ErrorResponseFactory(Tracer tracer, ErrorTypeHttpStatusMapper statusMapper) {
        this.tracer = tracer;
        this.statusMapper = statusMapper;
    }

    public ResponseEntity<ErrorResponse> buildResponseEntity(ErrorType type) {

        Span span = tracer.currentSpan();
        String traceId = (span != null) ? span.context().traceId() : null;

        //Don't expose server errors to client, so body uses defaultMessage. Log real error.
        ErrorResponse body = new ErrorResponse(type.getCode(), type.getDefaultMessage(), traceId);

        return ResponseEntity
                .status(statusMapper.mapErrorTypeToHttpStatus(type))
                .body(body);
    }
}
