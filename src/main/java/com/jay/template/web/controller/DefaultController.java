package com.jay.template.web.controller;

import com.jay.template.error.ErrorType;
import com.jay.template.error.ApiException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultController {

    record DefaultResponse(String message) {}

    @GetMapping("/default")
    DefaultResponse get() {

        String userId = "12345";

        if (userId == null) {
            throw new ApiException(ErrorType.USER_ID_MISSING);
        }

        return new DefaultResponse("This works. User Id: " + userId);
    }
}
