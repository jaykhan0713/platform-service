package com.jay.template.web.filter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.jay.template.helper.YamlBinder;
import com.jay.template.infra.request.IdentityContextHolder;
import com.jay.template.infra.request.IdentityContextSnapshot;
import com.jay.template.web.request.HttpProperties;

import static org.junit.jupiter.api.Assertions.*;

class IdentityRequestFilterTest {

    private static final String HTTP_PROPS_KEY = "app.http";

    private static HttpProperties httpProps;

    @BeforeAll
    static void initClass() throws Exception {
        YamlBinder binder = new YamlBinder();
        httpProps = binder.bind(HTTP_PROPS_KEY, HttpProperties.class);
    }

    @BeforeEach
    void setUp() {
        IdentityContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        IdentityContextHolder.clear();
    }

    @Test
    void filterChainIsCalled() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        IdentityRequestFilter filter = new IdentityRequestFilter(httpProps);

        filter.doFilter(request, response, filterChain);

        assertSame(request, filterChain.getRequest());
        assertSame(response, filterChain.getResponse());
    }

    @Test
    void identityContextSnapshotIsSetAndClears() throws ServletException, IOException {
        String userId = "user-001";
        String requestId = "req-001";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(httpProps.headers().userId(), userId);
        request.addHeader(httpProps.headers().requestId(), requestId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain assertingChain = (req, res) -> {
            IdentityContextSnapshot ctx = IdentityContextHolder.getContext();
            assertNotNull(ctx);
            assertNotSame(IdentityContextSnapshot.EMPTY, ctx);
            assertEquals(userId, ctx.identity().userId());
            assertEquals(requestId, ctx.identity().requestId());
        };

        IdentityRequestFilter filter = new IdentityRequestFilter(httpProps);

        filter.doFilter(request, response, assertingChain);

        IdentityContextSnapshot context = IdentityContextHolder.getContext();
        assertSame(IdentityContextSnapshot.EMPTY, context);
    }

    @Test
    void clearsContextWhenChainThrows() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(httpProps.headers().userId(), "user-001");
        request.addHeader(httpProps.headers().requestId(), "req-001");
        MockHttpServletResponse response = new MockHttpServletResponse();

        IdentityRequestFilter filter = new IdentityRequestFilter(httpProps);

        FilterChain throwingChain = (req, res) -> { throw new ServletException("error"); };

        assertThrows(ServletException.class, () -> filter.doFilter(request, response, throwingChain));
        assertSame(IdentityContextSnapshot.EMPTY, IdentityContextHolder.getContext());
    }

}