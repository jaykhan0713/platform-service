package com.jay.template.web.filter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import com.jay.template.helper.YamlBinder;
import com.jay.template.infra.logging.MdcProperties;
import com.jay.template.infra.logging.MetaDataLogger;

import com.jay.template.web.request.HttpProperties;
import jakarta.servlet.ServletException;
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

class MdcRequestFilterTest {

    private static final String HTTP_PROPS_KEY = "app.http";
    private static final String MDC_PROPS_KEY = "app.logging.mdc";
    private static final Logger META_DATA_LOGGER = (Logger) LoggerFactory.getLogger(MetaDataLogger.class);

    private static HttpProperties httpProps;
    private static MdcProperties mdcProps;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeAll
    static void initClass() throws Exception {
        YamlBinder binder = new YamlBinder();
        httpProps = binder.bind(HTTP_PROPS_KEY, HttpProperties.class);
        mdcProps = binder.bind(MDC_PROPS_KEY, MdcProperties.class);
    }

    @BeforeEach
    void setUp() {
        MDC.clear();
        listAppender = new ListAppender<>() {
            @Override
            protected void append(ILoggingEvent eventObject) {
                //getMDCPropertyMap lazy loads so have to ensure we have the MDC snapshot before it clears.
                eventObject.getMDCPropertyMap();
                super.append(eventObject);
            }
        };
        listAppender.start();

        META_DATA_LOGGER.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        META_DATA_LOGGER.detachAppender(listAppender);
    }

    @Test
    void filterChainIsCalled() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        MdcRequestFilter filter = new MdcRequestFilter(httpProps, mdcProps);

        filter.doFilter(request, response, filterChain);

        assertSame(request, filterChain.getRequest());
        assertSame(response, filterChain.getResponse());
    }

    @Test
    void mdcFieldsArePopulatedAndCleared() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain filterChain = new MockFilterChain();

        MdcRequestFilter filter = new MdcRequestFilter(httpProps, mdcProps);

        String requestId = "rid-001";
        String userId = "uid-001";
        String requestUri = "/test";

        request.addHeader(httpProps.headers().userId(), userId);
        request.addHeader(httpProps.headers().requestId(), requestId);
        request.setRequestURI(requestUri);
        request.setMethod(HttpMethod.GET.name());
        response.setStatus(HttpStatus.OK.value());

        filter.doFilter(request, response, filterChain);

        assertEquals(1, listAppender.list.size());
        assertEquals("", listAppender.list.getFirst().getFormattedMessage());

        Map<String, String> expected = Map.of(
                mdcProps.userId(), userId,
                mdcProps.requestId(), requestId,
                mdcProps.kind(), httpProps.kind(),
                mdcProps.name(), requestUri,
                mdcProps.method(), HttpMethod.GET.name(),
                mdcProps.status(), String.valueOf(HttpStatus.OK.value()),
                mdcProps.durationMs(), "0");

        Map<String, String> actual = listAppender.list.getFirst().getMDCPropertyMap();

        assertEquals(expected.size(), actual.size());
        assertTrue(expected.entrySet().stream()
                .allMatch(e -> {
                    if (e.getKey().equals(mdcProps.durationMs())) {
                        return Integer.parseInt(actual.get(e.getKey())) >= 0;
                    }
                    return actual.get(e.getKey()).equals(e.getValue());
                }));

        assertNull(MDC.getCopyOfContextMap());
    }
}