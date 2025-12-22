package com.jay.template.web.error;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.jay.template.app.error.ErrorType;

import static com.jay.template.app.error.ErrorType.BAD_REQUEST;
import static com.jay.template.app.error.ErrorType.INTERNAL_SERVER_ERROR;
import static com.jay.template.app.error.ErrorType.TOO_MANY_REQUESTS;
import static com.jay.template.app.error.ErrorType.USER_ID_MISSING;

@Component
class ErrorTypeHttpStatusMapper {

    final Map<ErrorType,HttpStatus> map = createMap();

    HttpStatus mapErrorTypeToHttpStatus(ErrorType type) {
        return map.getOrDefault(type, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<ErrorType, HttpStatus> createMap() {

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
