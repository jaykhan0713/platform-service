package com.jay.template.web.mvc.error;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import com.jay.template.web.error.ErrorTypeHttpStatusMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

import com.jay.template.app.error.ErrorType;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import static com.jay.template.app.error.ErrorType.BAD_REQUEST;
import static com.jay.template.app.error.ErrorType.INTERNAL_SERVER_ERROR;
import static com.jay.template.app.error.ErrorType.TOO_MANY_REQUESTS;
import static com.jay.template.app.error.ErrorType.USER_ID_MISSING;

class ErrorTypeHttpStatusMapperTest {

    private static final List<Arguments> STATUS_MAPPINGS = List.of(
            // 400s
            arguments(BAD_REQUEST, HttpStatus.BAD_REQUEST),
            arguments(USER_ID_MISSING, HttpStatus.BAD_REQUEST),
            arguments(TOO_MANY_REQUESTS, HttpStatus.TOO_MANY_REQUESTS),

            // 500s
            arguments(INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR)
    );

    private static Stream<Arguments> statusMappings() {
        return STATUS_MAPPINGS.stream();
    }

    private final ErrorTypeHttpStatusMapper mapper = new ErrorTypeHttpStatusMapper();

    //Ensures any time a new ErrorType is added, an explicit mapping exists.
    @Test
    void allErrorTypesHaveHttpStatusMapping() {
        assertEquals(
                EnumSet.allOf(ErrorType.class),
                mapper.typeToStatusMap.keySet(),
                "New ErrorType added without HTTP status mapping"
        );
    }

    // ensures any new ErrorTypes added map to the correct Http Status.
    @ParameterizedTest
    @MethodSource("statusMappings")
    void errorTypeMapsToCorrectHttpStatus(ErrorType errorType, HttpStatus httpStatus) {
        assertEquals(httpStatus, mapper.mapErrorTypeToHttpStatus(errorType));
    }

}