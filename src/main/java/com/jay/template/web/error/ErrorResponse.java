package com.jay.template.web.error;

import com.jay.template.error.ErrorType;

record ErrorResponse(String code, String message) {
    public static ErrorResponse from(ErrorType type, String message) {
        return new ErrorResponse(type.getCode(), message);
    }
}
