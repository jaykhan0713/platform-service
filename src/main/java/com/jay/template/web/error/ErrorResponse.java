package com.jay.template.web.error;

public record ErrorResponse(String code, String message, String gatewayTraceId) {
    public static ErrorResponse from(ErrorType type, String message, String gatewayTraceId) {
        return new ErrorResponse(type.getCode(), message, gatewayTraceId);
    }
}
