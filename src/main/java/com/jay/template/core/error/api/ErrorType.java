package com.jay.template.core.error.api;

public enum ErrorType {
    //Server errors
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Internal Server Error"),
    DEPENDENCY_UNAVAILABLE("DEPENDENCY_UNAVAILABLE", "Dependency Unavailable"),

    //Client errors
    BAD_REQUEST("BAD_REQUEST", "Bad Request"),
    USER_ID_MISSING("USER_ID_MISSING", "User ID is missing"),
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS", "Too many requests");


    private final String code;
    private final String message; //these messages are for human readability of the consumer

    ErrorType(String code, String defaultMessage) {
        this.code = code;
        this.message = defaultMessage;
    }

    public String code() {
        return code;
    }

    public String defaultMessage() {
        return message;
    }
}
