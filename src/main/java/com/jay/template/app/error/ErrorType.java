package com.jay.template.app.error;

public enum ErrorType {
    BAD_REQUEST("BAD_REQUEST", "Bad Request"),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Internal Server Error"),
    USER_ID_MISSING("USER_ID_MISSING", "User ID is missing");

    private final String code;
    private final String message;

    ErrorType(String code, String defaultMessage) {
        this.code = code;
        this.message = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return message;
    }
}
