package com.jay.template.infra.http.client.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.jay.template.bootstrap.outbound.http.properties.OutboundHttpProperties;
import com.jay.template.infra.outbound.http.client.resiliency.ResiliencyChainBuilder;
import com.jay.template.infra.outbound.http.client.rest.RestClientFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import com.jay.template.helper.YamlBinder;
import com.jay.template.infra.outbound.http.client.interceptor.IdentityHeaderInterceptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RestClientFactoryTest {

    private static final String OUTBOUND_HTTP_KEY = "platform.http";

    private static OutboundHttpProperties props;

    @BeforeAll
    static void initClass() throws Exception {
        props = new YamlBinder().bind(OUTBOUND_HTTP_KEY, OutboundHttpProperties.class);
    }

    @Test
    void throwsWhenClientConfigMissing() {

        RestClientFactory factory = new RestClientFactory(
                mock(RestClient.Builder.class),
                mock(ResiliencyChainBuilder.class),
                mock(RequestInterceptorRegistry.class),
                props
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> factory.buildClient("missing"));

        assertEquals("Missing config: platform.http.clients.missing",
                ex.getMessage());
    }

    @Test
    void buildsClientWithResiliencyAndInterceptors() {

        String clientName = "ping";

        ResiliencyChainBuilder resiliencyDecorator = mock(ResiliencyChainBuilder.class);
        ClientHttpRequestFactory decoratedFactory = mock(ClientHttpRequestFactory.class);
        when(resiliencyDecorator.applyBulkhead(any(), any(), any(), eq(clientName))).thenReturn(decoratedFactory);

        RequestInterceptorRegistry requestInterceptorRegistry = mock(RequestInterceptorRegistry.class);

        ClientHttpRequestInterceptor identityHeaderInterceptor = mock(IdentityHeaderInterceptor.class);
        ClientHttpRequestInterceptor otherInterceptor = mock(ClientHttpRequestInterceptor.class);

        List<ClientHttpRequestInterceptor> interceptors = List.of(identityHeaderInterceptor, otherInterceptor);

        // so that applied adds interceptors to the end of applied below.
        when(requestInterceptorRegistry
                .createInterceptors(anyList())).thenReturn(interceptors);

        RestClient.Builder rootBuilder = mock(RestClient.Builder.class);
        RestClient.Builder builder = mock(RestClient.Builder.class);
        RestClient restClient = mock(RestClient.class);

        when(rootBuilder.clone()).thenReturn(builder);

        when(builder.requestFactory(any())).thenReturn(builder);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.requestInterceptors(any())).thenReturn(builder);
        when(builder.build()).thenReturn(restClient);

        RestClientFactory factory = new RestClientFactory(
                rootBuilder, resiliencyDecorator, requestInterceptorRegistry, props
        );

        RestClient result = factory.buildClient(clientName);

        assertSame(restClient, result);

        verify(rootBuilder).clone();
        verify(builder).requestFactory(same(decoratedFactory));
        verify(resiliencyDecorator).applyBulkhead(any(), any(), any(), eq(clientName));
        verify(builder).baseUrl(eq(props.clients().get(clientName).baseUrl()));
        verify(requestInterceptorRegistry).createInterceptors(anyList());
        verify(builder).build();

        @SuppressWarnings("unchecked")
        //ArgumentCaptor<T> where T means what was passed into the method
        ArgumentCaptor<Consumer<List<ClientHttpRequestInterceptor>>> captor =
                ArgumentCaptor.forClass(Consumer.class);

        verify(builder).requestInterceptors(captor.capture()); //verifies method was called once, then capture
        ClientHttpRequestInterceptor existing = mock(ClientHttpRequestInterceptor.class);
        List<ClientHttpRequestInterceptor> applied = new ArrayList<>();
        applied.add(existing);
        captor.getValue().accept(applied);

        assertSame(existing, applied.get(0));
        assertSame(identityHeaderInterceptor, applied.get(1));
        assertSame(otherInterceptor, applied.get(2));
    }
}
