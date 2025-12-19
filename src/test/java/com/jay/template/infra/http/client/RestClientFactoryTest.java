package com.jay.template.infra.http.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import com.jay.template.helper.YamlBinder;
import com.jay.template.infra.http.interceptor.IdentityHeaderInterceptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RestClientFactoryTest {

    private static final String OUTBOUND_HTTP_KEY = "app.outbound.http";

    private static HttpProperties props;

    @BeforeAll
    static void initClass() throws Exception {
        props = new YamlBinder().bind(OUTBOUND_HTTP_KEY, HttpProperties.class);
    }

    @Test
    void throwsWhenClientConfigMissing() {

        RestClientFactory factory = new RestClientFactory(props,
                mock(RequestInterceptorRegistry.class), mock(RestClient.Builder.class));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> factory.buildClient("missing"));

        assertEquals("Missing outbound http client config: app.outbound.http.clients.missing",
                ex.getMessage());
    }

    @Test
    void clientRegistersInterceptorsInOrderAndBuildsClient() {

        String clientName = "ping";

        RequestInterceptorRegistry requestInterceptorRegistry = mock(RequestInterceptorRegistry.class);

        ClientHttpRequestInterceptor identityHeaderInterceptor = mock(IdentityHeaderInterceptor.class);
        ClientHttpRequestInterceptor otherInterceptor = mock(ClientHttpRequestInterceptor.class);

        List<ClientHttpRequestInterceptor> interceptors = List.of(identityHeaderInterceptor, otherInterceptor);

        // so that applied adds interceptors to the end of applied below.
        when(requestInterceptorRegistry
                .getInterceptors(anyList())).thenReturn(interceptors);

        RestClient.Builder rootBuilder = mock(RestClient.Builder.class);
        RestClient.Builder builder = mock(RestClient.Builder.class);
        RestClient restClient = mock(RestClient.class);

        when(rootBuilder.clone()).thenReturn(builder);

        when(builder.requestFactory(any())).thenReturn(builder);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.requestInterceptors(any())).thenReturn(builder);
        when(builder.build()).thenReturn(restClient);

        RestClientFactory factory = new RestClientFactory(props, requestInterceptorRegistry, rootBuilder);

        RestClient result = factory.buildClient(clientName);

        assertSame(restClient, result);

        verify(rootBuilder).clone();
        verify(builder).requestFactory(any());
        verify(builder).baseUrl(anyString());
        verify(builder).build();

        @SuppressWarnings("unchecked")
        //ArgumentCaptor<T> where T means what was passed into the method
        ArgumentCaptor<Consumer<List<ClientHttpRequestInterceptor>>> captor =
                ArgumentCaptor.forClass(Consumer.class);

        verify(builder).requestInterceptors(captor.capture()); //verifies method was called once, then capture
        List<ClientHttpRequestInterceptor> applied = new ArrayList<>();
        applied.add(mock(ClientHttpRequestInterceptor.class)); // make sure items 2 and 3 are added to end of list.
        captor.getValue().accept(applied); // invoke the Consumer's accept

        assertSame(identityHeaderInterceptor, applied.get(1));
        assertSame(otherInterceptor, applied.get(2));

    }
}
