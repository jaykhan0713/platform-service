package com.jay.template.web.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jay.template.error.ErrorType;
import com.jay.template.error.ApiException;
import com.jay.template.infra.identity.RequestIdentity;
import com.jay.template.web.identity.HttpHeaderIdentityExtractor;

@RestController
public class DefaultController {

    static final String DEFAULT_MESSAGE = "This works";

    private final HttpHeaderIdentityExtractor extractor;

    public DefaultController(HttpHeaderIdentityExtractor extractor) {
        this.extractor = extractor;
    }

    @GetMapping("/default")
    DefaultResponse get(HttpServletRequest request) {
        RequestIdentity identity = extractor.extract(request);

        if (Strings.isBlank(identity.userId())) {
            throw new ApiException(ErrorType.USER_ID_MISSING);
        }

        return new DefaultResponse(DEFAULT_MESSAGE, identity.requestId());
    }

    record DefaultResponse(String message, String requestId) {}
}
