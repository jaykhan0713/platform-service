package com.jay.template.controller;

import com.jay.template.logging.mdc.MdcRetriever;
import com.jay.template.web.error.ErrorType;
import com.jay.template.web.error.ApiException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultController {

    record DefaultResponse(String message) {}

    private final MdcRetriever mdcRetriever;

    public DefaultController(MdcRetriever mdcRetriever) {
        this.mdcRetriever = mdcRetriever;
    }

    @GetMapping("/default")
    DefaultResponse get() {
        String userId = mdcRetriever.getUserId();

        if (userId == null) {
            throw new ApiException(ErrorType.USER_ID_MISSING);
        }

        return new DefaultResponse("This works. User Id: " + userId);
    }

    @GetMapping("/test")
    DefaultResponse test() {
        String userId = mdcRetriever.getUserId();

        if (userId == null) {
            throw new IllegalStateException("Test IllegalStateException");
        }

        return new DefaultResponse("This works.");
    }
}
