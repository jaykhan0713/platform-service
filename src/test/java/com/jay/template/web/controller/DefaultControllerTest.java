package com.jay.template.web.controller;

import com.jay.template.error.ApiException;
import com.jay.template.helper.YamlBinder;
import com.jay.template.infra.identity.RequestIdentity;
import com.jay.template.web.identity.HttpHeaderIdentityExtractor;
import com.jay.template.web.request.HttpProperties;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class DefaultControllerTest {

    static HttpProperties props;

    @BeforeAll
    static void setup() throws Exception {
        YamlBinder binder = new YamlBinder();
        props = binder.bind("app.http", HttpProperties.class);
    }

    @Test
    void getReturnsResponse() {
        HttpHeaderIdentityExtractor extractor = Mockito.mock(HttpHeaderIdentityExtractor.class);
        String userId = "user-001";
        String requestId = "request-001";

        RequestIdentity identity = new RequestIdentity(userId, requestId);
        Mockito.when(extractor.extract(Mockito.any(HttpServletRequest.class))).thenReturn(identity);

        DefaultController controller = new DefaultController(extractor);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(props.headers().userId(), userId);
        request.addHeader(props.headers().requestId(), requestId);

        DefaultController.DefaultResponse defaultResponse = controller.get(request);

        assertEquals(DefaultController.DEFAULT_MESSAGE, defaultResponse.message());
        assertEquals(requestId, defaultResponse.requestId());
    }

    @Test
    void getThrowsWhenUserIdMissing() {
        HttpHeaderIdentityExtractor extractor = Mockito.mock(HttpHeaderIdentityExtractor.class);

        String requestId = "request-001";

        RequestIdentity identity = new RequestIdentity(null, requestId);
        Mockito.when(extractor.extract(Mockito.any(HttpServletRequest.class))).thenReturn(identity);

        DefaultController controller = new DefaultController(extractor);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(props.headers().requestId(), requestId);

        assertThrows(ApiException.class, () -> controller.get(request));
    }
}