package com.jay.template.web.controller;

import com.jay.template.infra.request.IdentityContextHolder;
import com.jay.template.infra.request.IdentityContextSnapshot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jay.template.api.v1.sample.model.SampleResponse;
import com.jay.template.error.ApiException;
import com.jay.template.infra.request.Identity;

import static org.junit.jupiter.api.Assertions.*;

class SampleControllerTest {

    @BeforeEach
    void setUp() {
        IdentityContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        IdentityContextHolder.clear();
    }

    @Test
    void getReturnsResponse() {
        String userId = "user-001";
        String requestId = "request-001";

        Identity identity = new Identity(userId, requestId);
        IdentityContextHolder.setContext(IdentityContextSnapshot.of(identity));

        SampleController controller = new SampleController();

        SampleResponse sampleResponse = controller.get();

        assertEquals(SampleController.SUCCESS_MESSAGE, sampleResponse.message());
        assertEquals(requestId, sampleResponse.requestId());
    }

    @Test
    void getThrowsWhenIdentityUserIdMissing() {

        String requestId = "request-001";

        Identity identity = new Identity(null, requestId);
        IdentityContextHolder.setContext(IdentityContextSnapshot.of(identity));

        SampleController controller = new SampleController();

        assertThrows(ApiException.class, controller::get);
    }
}