package com.jay.template.web.controller;

import jakarta.servlet.http.HttpServletRequest;

import com.jay.template.infra.request.IdentityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jay.template.api.v1.sample.model.SampleResponse;
import com.jay.template.error.ErrorType;
import com.jay.template.error.ApiException;
import com.jay.template.infra.request.Identity;

@RestController
public class SampleController {

    static final String SUCCESS_MESSAGE = "Sample Endpoint Success.";


    @GetMapping("/v1/sample")
    SampleResponse get() {

        Identity identity = IdentityContextHolder.getContext().identity();

        if (!StringUtils.hasText(identity.userId())) {
            throw new ApiException(ErrorType.USER_ID_MISSING);
        }

        return new SampleResponse(SUCCESS_MESSAGE, identity.requestId());
    }
}
