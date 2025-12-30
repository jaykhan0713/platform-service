package com.jay.template.core.error.api;

public enum ErrorType {
    //Server errors
    INTERNAL_SERVER_ERROR("Internal Server Error"),
    DEPENDENCY_UNAVAILABLE( "Dependency Unavailable"),

    //Client errors
    BAD_REQUEST("Bad Request"),
    USER_ID_MISSING( "User ID is missing"),
    TOO_MANY_REQUESTS("Too many requests");


    private final String message; //these messages are for human readability of the consumer

    ErrorType(String defaultMessage) {
        this.message = defaultMessage;
    }

    public String defaultMessage() {
        return message;
    }
}
