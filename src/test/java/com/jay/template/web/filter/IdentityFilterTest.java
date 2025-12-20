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
import com.jay.template.infra.identity.IdentityContextHolder;
import com.jay.template.infra.identity.IdentityContextSnapshot;
import com.jay.template.infra.identity.IdentityProperties;

import static org.junit.jupiter.api.Assertions.*;

class IdentityFilterTest {

    private static final String IDENTITY_PROPS_KEY = "app.identity";

    private static IdentityProperties props;

    @BeforeAll
    static void initClass() throws Exception {
        YamlBinder binder = new YamlBinder();
        props = binder.bind(IDENTITY_PROPS_KEY, IdentityProperties.class);
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

        IdentityFilter filter = new IdentityFilter(props);

        filter.doFilter(request, response, filterChain);

        assertSame(request, filterChain.getRequest());
        assertSame(response, filterChain.getResponse());
    }

    @Test
    void identityContextSnapshotIsSetAndClears() throws ServletException, IOException {
        String userId = "user-001";
        String requestId = "req-001";

        var headerKeys = props.http().headers();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(headerKeys.userId(), userId);
        request.addHeader(headerKeys.requestId(), requestId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain assertingChain = (req, res) -> {
            IdentityContextSnapshot ctx = IdentityContextHolder.getContext();
            assertNotNull(ctx);
            assertNotSame(IdentityContextSnapshot.EMPTY, ctx);
            assertEquals(userId, ctx.identity().userId());
            assertEquals(requestId, ctx.identity().requestId());
        };

        IdentityFilter filter = new IdentityFilter(props);

        filter.doFilter(request, response, assertingChain);

        IdentityContextSnapshot context = IdentityContextHolder.getContext();
        assertSame(IdentityContextSnapshot.EMPTY, context);
    }

    @Test
    void clearsContextWhenChainThrows() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        var headerKeys = props.http().headers();
        request.addHeader(headerKeys.userId(), "user-001");
        request.addHeader(headerKeys.requestId(), "req-001");
        MockHttpServletResponse response = new MockHttpServletResponse();

        IdentityFilter filter = new IdentityFilter(props);

        FilterChain throwingChain = (req, res) -> { throw new ServletException("error"); };

        assertThrows(ServletException.class, () -> filter.doFilter(request, response, throwingChain));
        assertSame(IdentityContextSnapshot.EMPTY, IdentityContextHolder.getContext());
    }

}