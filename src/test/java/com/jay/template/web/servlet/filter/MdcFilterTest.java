package com.jay.template.web.servlet.filter;

import java.io.IOException;
import java.util.Map;
import jakarta.servlet.ServletException;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.jay.template.infra.identity.Identity;
import com.jay.template.infra.identity.IdentityContextHolder;
import com.jay.template.infra.identity.IdentityContextSnapshot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.jay.template.helper.YamlBinder;
import com.jay.template.infra.logging.MdcProperties;

import static org.junit.jupiter.api.Assertions.*;

class MdcFilterTest {

    private static final String MDC_PROPS_KEY = "platform.logging.mdc";
    private static final Logger MDC_FILTER_LOGGER = (Logger) LoggerFactory.getLogger(MdcFilter.class);

    private static MdcProperties mdcProps;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeAll
    static void initClass() throws Exception {
        YamlBinder binder = new YamlBinder();
        mdcProps = binder.bind(MDC_PROPS_KEY, MdcProperties.class);
    }

    @BeforeEach
    void setUp() {
        MDC.clear();
        IdentityContextHolder.clear();

        listAppender = new ListAppender<>() {
            @Override
            protected void append(ILoggingEvent eventObject) {
                //getMDCPropertyMap lazy loads so have to ensure we have the MDC snapshot before it clears.
                eventObject.getMDCPropertyMap();
                super.append(eventObject);
            }
        };
        listAppender.start();

        MDC_FILTER_LOGGER.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        IdentityContextHolder.clear();

        MDC_FILTER_LOGGER.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void filterChainIsCalled() throws ServletException, IOException {
        MdcFilter filter = new MdcFilter(mdcProps);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertSame(request, filterChain.getRequest());
        assertSame(response, filterChain.getResponse());
    }

    @Test
    void mdcFieldsArePopulatedAndClears() throws ServletException, IOException {
        MdcFilter filter = new MdcFilter(mdcProps);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        String userId = "user-001";
        String requestId = "req-001";
        String requestUri = "/test";

        IdentityContextHolder.context(IdentityContextSnapshot.of(new Identity(userId, requestId)));

        request.setRequestURI(requestUri);
        request.setMethod(HttpMethod.GET.name());
        response.setStatus(HttpStatus.OK.value());

        filter.doFilter(request, response, filterChain);

        assertEquals(1, listAppender.list.size());
        assertEquals("", listAppender.list.getFirst().getFormattedMessage());

        Map<String, String> expected = Map.of(
                mdcProps.userId(), userId,
                mdcProps.requestId(), requestId,
                mdcProps.kind(), mdcProps.kindValues().http(),
                mdcProps.name(), requestUri,
                mdcProps.method(), HttpMethod.GET.name(),
                mdcProps.status(), String.valueOf(HttpStatus.OK.value()));

        Map<String, String> actual = listAppender.list.getFirst().getMDCPropertyMap();

        assertEquals(expected.size() + 1, actual.size()); // +1 for durationMs
        assertTrue(expected.entrySet().stream().allMatch(
                e -> actual.get(e.getKey()).equals(e.getValue())));
        assertTrue(Integer.parseInt(actual.get(mdcProps.durationMs())) >= 0);
        assertNull(MDC.getCopyOfContextMap());
    }

    @Test
    void clearsWhenThrows() throws IOException {
        MdcFilter filter = new MdcFilter(mdcProps);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String userId = "user-001";
        String requestId = "req-001";
        String requestUri = "/test";

        IdentityContextHolder.context(IdentityContextSnapshot.of(new Identity(userId, requestId)));

        request.setRequestURI(requestUri);
        request.setMethod(HttpMethod.GET.name());
        response.setStatus(HttpStatus.OK.value());

        assertThrows(ServletException.class, () ->
                filter.doFilter(request, response, (_, _) -> {
                    throw new ServletException("error");
                })
        );

        // MDC must be cleared even when downstream throws.
        assertNull(MDC.getCopyOfContextMap());

        // Your filter logs one completion line in finally, so we should still have exactly one event.
        assertEquals(1, listAppender.list.size());
        assertEquals("", listAppender.list.getFirst().getFormattedMessage());

        Map<String, String> expected = Map.of(
                mdcProps.userId(), userId,
                mdcProps.requestId(), requestId,
                mdcProps.kind(), mdcProps.kindValues().http(),
                mdcProps.name(), requestUri,
                mdcProps.method(), HttpMethod.GET.name(),
                mdcProps.status(), String.valueOf(HttpStatus.OK.value()));

        Map<String, String> actual = listAppender.list.getFirst().getMDCPropertyMap();

        assertEquals(expected.size() + 1, actual.size()); // +1 for durationMs
        assertTrue(expected.entrySet().stream().allMatch(
                e -> actual.get(e.getKey()).equals(e.getValue())));

        // Duration should exist and be non-negative.
        assertTrue(Integer.parseInt(actual.get(mdcProps.durationMs())) >= 0);

        assertNull(MDC.getCopyOfContextMap());
    }
}