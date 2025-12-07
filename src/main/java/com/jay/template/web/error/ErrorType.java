package com.jay.template.web.error;

import org.springframework.http.HttpStatus;

public enum ErrorType {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Bad Request"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Internal Server Error"),
    USER_ID_MISSING(HttpStatus.BAD_REQUEST, "USER_ID_MISSING", "User ID is missing");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorType(HttpStatus status, String code, String defaultMessage) {
        this.status = status;
        this.code = code;
        this.message = defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return message;
    }
}
