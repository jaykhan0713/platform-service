package com.jay.template.core.error.api;

import java.util.Objects;

// app layer exception
public final class ApiException extends RuntimeException {

    private final ErrorType type;

    public ApiException(ErrorType type) {
        this.type = Objects.requireNonNull(type);
    }

    public ApiException(ErrorType type, String customMessage) {
        super(Objects.requireNonNull(customMessage));
        this.type = Objects.requireNonNull(type);
    }

    public ApiException(ErrorType type, Throwable cause) {
        super(cause);
        this.type = Objects.requireNonNull(type);
    }

    public ApiException(ErrorType type, String customMessage, Throwable cause) {
        super(Objects.requireNonNull(customMessage), cause);
        this.type = Objects.requireNonNull(type);
    }

    public ErrorType type() {
        return type;
    }
}
