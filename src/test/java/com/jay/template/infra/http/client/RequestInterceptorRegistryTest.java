package com.jay.template.infra.http.client;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.jay.template.infra.http.interceptor.OutboundRequestInterceptorFeature;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestInterceptorRegistryTest {

    @Test
    void getInterceptorsPreservesOrderOfInputList() {
        OutboundRequestInterceptorFeature feature1 = mock(OutboundRequestInterceptorFeature.class);
        String key1 = "key1";
        when(feature1.key()).thenReturn(key1);
        ClientHttpRequestInterceptor interceptor1 = mock(ClientHttpRequestInterceptor.class);
        when(feature1.interceptor()).thenReturn(interceptor1);

        OutboundRequestInterceptorFeature feature2 = mock(OutboundRequestInterceptorFeature.class);
        String key2 = "key2";
        when(feature2.key()).thenReturn(key2);
        ClientHttpRequestInterceptor interceptor2 = mock(ClientHttpRequestInterceptor.class);
        when(feature2.interceptor()).thenReturn(interceptor2);


        List<OutboundRequestInterceptorFeature> features = List.of(feature2, feature1);
        RequestInterceptorRegistry registry = new RequestInterceptorRegistry(features);
        List<ClientHttpRequestInterceptor> interceptors = registry.getInterceptors(List.of(key1, key2));

        assertSame(interceptor1, interceptors.get(0));
        assertSame(interceptor2, interceptors.get(1));
    }

    @Test
    void getInterceptorsThrowsWhenEntryNotFound() {
        OutboundRequestInterceptorFeature feature1 = mock(OutboundRequestInterceptorFeature.class);
        String key1 = "key1";
        when(feature1.key()).thenReturn(key1);
        ClientHttpRequestInterceptor interceptor1 = mock(ClientHttpRequestInterceptor.class);
        when(feature1.interceptor()).thenReturn(interceptor1);

        OutboundRequestInterceptorFeature feature2 = mock(OutboundRequestInterceptorFeature.class);
        String key2 = "key2";
        when(feature2.key()).thenReturn(key2);
        ClientHttpRequestInterceptor interceptor2 = mock(ClientHttpRequestInterceptor.class);
        when(feature2.interceptor()).thenReturn(interceptor2);

        String key3 = "key3";

        List<OutboundRequestInterceptorFeature> features = List.of(feature2, feature1);
        RequestInterceptorRegistry registry = new RequestInterceptorRegistry(features);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> registry.getInterceptors(List.of(key1, key2, key3)));

        assertEquals("No request interceptor found for key: " + key3, ex.getMessage());
    }

    @Test
    void getInterceptorsThrowsWhenInterceptorListHasDuplicateKeys() {
        OutboundRequestInterceptorFeature feature1 = mock(OutboundRequestInterceptorFeature.class);
        String key1 = "key1";
        when(feature1.key()).thenReturn(key1);
        ClientHttpRequestInterceptor interceptor1 = mock(ClientHttpRequestInterceptor.class);
        when(feature1.interceptor()).thenReturn(interceptor1);

        OutboundRequestInterceptorFeature feature2 = mock(OutboundRequestInterceptorFeature.class);
        String key2 = "key2";
        when(feature2.key()).thenReturn(key2);
        ClientHttpRequestInterceptor interceptor2 = mock(ClientHttpRequestInterceptor.class);
        when(feature2.interceptor()).thenReturn(interceptor2);


        List<OutboundRequestInterceptorFeature> features = List.of(feature2, feature1);
        RequestInterceptorRegistry registry = new RequestInterceptorRegistry(features);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> registry.getInterceptors(List.of(key1, key2, key2)));

        assertEquals("Duplicate request interceptor for key: " + key2, ex.getMessage());
    }

    @Test
    void constructorThrowsExceptionWhenFeaturesHaveDuplicateKeys() {
        OutboundRequestInterceptorFeature feature1 = mock(OutboundRequestInterceptorFeature.class);
        String key = "key";
        when(feature1.key()).thenReturn(key);
        ClientHttpRequestInterceptor interceptor1 = mock(ClientHttpRequestInterceptor.class);
        when(feature1.interceptor()).thenReturn(interceptor1);

        OutboundRequestInterceptorFeature feature2 = mock(OutboundRequestInterceptorFeature.class);

        when(feature2.key()).thenReturn(key);
        ClientHttpRequestInterceptor interceptor2 = mock(ClientHttpRequestInterceptor.class);
        when(feature2.interceptor()).thenReturn(interceptor2);

        List<OutboundRequestInterceptorFeature> features = List.of(feature2, feature1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new RequestInterceptorRegistry(features));

        assertEquals("Request interceptor features share duplicate key: " + key, ex.getMessage());
    }

}