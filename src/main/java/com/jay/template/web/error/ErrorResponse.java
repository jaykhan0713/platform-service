package com.jay.template.web.error;

import com.jay.template.infra.error.ErrorType;

record ErrorResponse(String code, String message, String correlationId) {
    public static ErrorResponse from(ErrorType type, String correlationId) {
        return new ErrorResponse(type.getCode(), type.getDefaultMessage(), correlationId);
    }
}
