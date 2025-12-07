package com.jay.template.web.error;

public class ApiException extends RuntimeException {

    private final ErrorType type;

    public ApiException(ErrorType type) {
        this(type, type.getDefaultMessage());
    }

    public ApiException(ErrorType type, String customMessage) {
        super(customMessage);
        this.type = type;
    }

    public ErrorType getType() {
        return type;
    }
}
