package com.jay.template.infra.http.interceptor;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import com.jay.template.helper.YamlBinder;
import com.jay.template.infra.identity.Identity;
import com.jay.template.infra.identity.IdentityContextHolder;
import com.jay.template.infra.identity.IdentityContextSnapshot;
import com.jay.template.infra.identity.IdentityProperties;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IdentityHeaderInterceptorTest {

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
    void addsIdentityHeadersAndDelegates() throws IOException {

        IdentityHeaderInterceptor interceptor = new IdentityHeaderInterceptor(props);

        Identity identity = new Identity("user-001", "req-001");
        IdentityContextHolder.setContext(IdentityContextSnapshot.of(identity));

        HttpRequest request = mock(HttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(execution.execute(any(), any())).thenReturn(response);

        byte[] body = new byte[0];

        ClientHttpResponse result = interceptor.intercept(request, body, execution);

        var headerKeys = props.http().headers();

        assertEquals(1, headers.get(headerKeys.userId()).size());
        assertEquals(identity.userId(), headers.getFirst(headerKeys.userId()));

        assertEquals(1, headers.get(headerKeys.requestId()).size());
        assertEquals(identity.requestId(), headers.getFirst(headerKeys.requestId()));

        verify(execution).execute(request, body);
        assertSame(response, result);
    }

    @Test
    void addsEmptyIdentityHeadersAndDelegates() throws IOException {

        IdentityHeaderInterceptor interceptor = new IdentityHeaderInterceptor(props);

        HttpRequest request = mock(HttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(execution.execute(any(), any())).thenReturn(response);

        byte[] body = new byte[0];

        ClientHttpResponse result = interceptor.intercept(request, body, execution);

        var headerKeys = props.http().headers();

        assertEquals(1, headers.get(headerKeys.userId()).size());
        assertEquals("", headers.getFirst(headerKeys.userId()));

        assertEquals(1, headers.get(headerKeys.requestId()).size());
        assertEquals("", headers.getFirst(headerKeys.requestId()));

        verify(execution).execute(request, body);
        assertSame(response, result);
    }

}