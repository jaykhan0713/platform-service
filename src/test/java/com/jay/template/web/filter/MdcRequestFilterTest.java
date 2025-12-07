package com.jay.template.web.filter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import com.jay.template.helper.YamlBinder;
import com.jay.template.logging.MetaDataLogger;
import com.jay.template.logging.mdc.MdcProperties;

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

    private static final String PROPS_KEY = "app.logging.mdc";
    private static final Logger META_DATA_LOGGER = (Logger) LoggerFactory.getLogger(MetaDataLogger.class);

    private static YamlBinder binder;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeAll
    static void initClass() throws IOException {
        binder = new YamlBinder();
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

        MdcProperties props = new MdcProperties();
        props.setHeaders(Map.of("test-header", "testHeader"));
        MdcRequestFilter filter = new MdcRequestFilter(props);

        filter.doFilter(request, response, filterChain);

        assertSame(request, filterChain.getRequest());
        assertSame(response, filterChain.getResponse());
    }

    @Test
    void filterChainBlankProps() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("test-header", " ");
        MockFilterChain filterChain = new MockFilterChain();

        MdcProperties props = new MdcProperties();
        props.setHeaders(Map.of("test-header", "testHeader"));
        props.setMethod(" ");
        props.setPath(" ");
        props.setStatus(" ");
        props.setDurationMs(" ");

        MdcRequestFilter filter = new MdcRequestFilter(props);

        filter.doFilter(request, response, filterChain);

        assertSame(request, filterChain.getRequest());
        assertSame(response, filterChain.getResponse());
    }

    @Test
    void mdcFieldsArePopulatedAndCleared() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain filterChain = new MockFilterChain();

        MdcProperties props = binder.bind(PROPS_KEY, MdcProperties.class);

        MdcRequestFilter filter = new MdcRequestFilter(props);

        String gatewayTraceId = "trace-001";
        String userId = "user-001";
        String requestUri = "/test";
        String gatewayTraceIdHeader = "x-gateway-trace-id";
        String userIdHeader = "x-user-id";

        request.addHeader(gatewayTraceIdHeader, gatewayTraceId);
        request.addHeader(userIdHeader, userId);
        request.setRequestURI(requestUri);
        request.setMethod(HttpMethod.GET.name());
        response.setStatus(HttpStatus.OK.value());

        filter.doFilter(request, response, filterChain);

        assertEquals(1, listAppender.list.size());
        assertEquals("request_complete", listAppender.list.getFirst().getFormattedMessage());

        Map<String, String> expected = Map.of(props.getHeaders().get(gatewayTraceIdHeader), gatewayTraceId,
                props.getHeaders().get(userIdHeader), userId,
                props.getPath(), requestUri,
                props.getMethod(), HttpMethod.GET.name(),
                props.getStatus(), String.valueOf(HttpStatus.OK.value()),
                props.getDurationMs(), "0");

        Map<String, String> actual = listAppender.list.getFirst().getMDCPropertyMap();

        assertEquals(expected.size(), actual.size());
        assertTrue(expected.entrySet().stream()
                .allMatch(e -> {
                    if (e.getKey().equals(props.getDurationMs())) {
                        return Integer.parseInt(actual.get(e.getKey())) >= 0;
                    }
                    return actual.get(e.getKey()).equals(e.getValue());
                }));

        assertNull(MDC.getCopyOfContextMap());
    }
}