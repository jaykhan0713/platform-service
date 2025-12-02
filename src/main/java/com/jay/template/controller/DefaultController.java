package com.jay.template.controller;

import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultController {

    record DefaultResponse(String message) {}

    @GetMapping("/")
    DefaultResponse get() {
        return new DefaultResponse("This works. User Id: " + MDC.get("userId"));
    }
}
