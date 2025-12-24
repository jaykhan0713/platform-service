package com.jay.template.web.mvc.controller.v1;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jay.template.api.v1.sample.SampleApi;
import com.jay.template.api.v1.sample.model.SampleResponse;
import com.jay.template.app.error.ApiException;
import com.jay.template.infra.identity.Identity;
import com.jay.template.infra.identity.IdentityContextHolder;

import static com.jay.template.app.error.ErrorType.USER_ID_MISSING;

@RestController
public class SampleController implements SampleApi {

    static final String SUCCESS_MESSAGE = "Sample Endpoint Success.";

    @Override
    @GetMapping("/api/v1/sample")
    public SampleResponse get() {

        Identity identity = IdentityContextHolder.context().identity();

        if (!StringUtils.hasText(identity.userId())) {
            throw new ApiException(USER_ID_MISSING);
        }

        return new SampleResponse(SUCCESS_MESSAGE, identity.requestId());
    }
}
