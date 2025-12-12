package com.jay.template.web.identity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.jay.template.helper.YamlBinder;
import com.jay.template.infra.identity.RequestIdentity;
import com.jay.template.web.request.HttpProperties;

import static org.junit.jupiter.api.Assertions.*;

class HttpHeaderIdentityExtractorTest {

    static HttpHeaderIdentityExtractor extractor;
    static HttpProperties props;

    @BeforeAll
    static void setup() throws Exception {
        YamlBinder binder = new YamlBinder();
        props = binder.bind("app.http", HttpProperties.class);
        extractor = new HttpHeaderIdentityExtractor(props);
    }

    @Test
    void shouldExtractRequest() {
        String userId = "user-001";
        String requestId = "request-001";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(props.headers().userId(), userId);
        request.addHeader(props.headers().requestId(), requestId);

        RequestIdentity identity = extractor.extract(request);

        assertEquals(userId, identity.userId());
        assertEquals(requestId, identity.requestId());
    }
}