package com.jay.template.web.error;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

import com.jay.template.core.error.api.ErrorType;

import static com.jay.template.core.error.api.ErrorType.BAD_REQUEST;
import static com.jay.template.core.error.api.ErrorType.INTERNAL_SERVER_ERROR;
import static com.jay.template.core.error.api.ErrorType.TOO_MANY_REQUESTS;
import static com.jay.template.core.error.api.ErrorType.USER_ID_MISSING;

final class ErrorTypeHttpStatusMapper {

    private static final Map<ErrorType,HttpStatus> TYPE_TO_STATUS_MAP = createMap();

    HttpStatus mapErrorTypeToHttpStatus(ErrorType type) {
        return TYPE_TO_STATUS_MAP.getOrDefault(type, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static Map<ErrorType, HttpStatus> createMap() {

        final Map<ErrorType, HttpStatus> map = new EnumMap<>(ErrorType.class);

        // 400s
        map.put(BAD_REQUEST, HttpStatus.BAD_REQUEST);
        map.put(USER_ID_MISSING, HttpStatus.BAD_REQUEST);
        map.put(TOO_MANY_REQUESTS, HttpStatus.TOO_MANY_REQUESTS);

        // 500s
        map.put(INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

        return Collections.unmodifiableMap(map);
    }
}
