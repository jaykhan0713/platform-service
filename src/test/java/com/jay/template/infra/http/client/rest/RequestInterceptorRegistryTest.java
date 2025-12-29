package com.jay.template.infra.http.client.rest;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestInterceptorRegistryTest {

    @Test
    void createInterceptorsPreservesOrderOfInputList() {
        RequestInterceptorFeature feature1 = mock(RequestInterceptorFeature.class);
        String key1 = "key1";
        when(feature1.key()).thenReturn(key1);
        ClientHttpRequestInterceptor interceptor1 = mock(ClientHttpRequestInterceptor.class);
        when(feature1.interceptor()).thenReturn(interceptor1);

        RequestInterceptorFeature feature2 = mock(RequestInterceptorFeature.class);
        String key2 = "key2";
        when(feature2.key()).thenReturn(key2);
        ClientHttpRequestInterceptor interceptor2 = mock(ClientHttpRequestInterceptor.class);
        when(feature2.interceptor()).thenReturn(interceptor2);


        List<RequestInterceptorFeature> features = List.of(feature2, feature1);
        RequestInterceptorRegistry registry = new RequestInterceptorRegistry(features);
        List<ClientHttpRequestInterceptor> interceptors = registry.createInterceptors(List.of(key1, key2));

        assertSame(interceptor1, interceptors.get(0));
        assertSame(interceptor2, interceptors.get(1));
    }

    @Test
    void createInterceptorsThrowsWhenEntryNotFound() {
        RequestInterceptorFeature feature1 = mock(RequestInterceptorFeature.class);
        String key1 = "key1";
        when(feature1.key()).thenReturn(key1);
        ClientHttpRequestInterceptor interceptor1 = mock(ClientHttpRequestInterceptor.class);
        when(feature1.interceptor()).thenReturn(interceptor1);

        RequestInterceptorFeature feature2 = mock(RequestInterceptorFeature.class);
        String key2 = "key2";
        when(feature2.key()).thenReturn(key2);
        ClientHttpRequestInterceptor interceptor2 = mock(ClientHttpRequestInterceptor.class);
        when(feature2.interceptor()).thenReturn(interceptor2);

        String key3 = "key3";

        List<RequestInterceptorFeature> features = List.of(feature2, feature1);
        RequestInterceptorRegistry registry = new RequestInterceptorRegistry(features);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> registry.createInterceptors(List.of(key1, key2, key3)));

        assertEquals("No request interceptor found for key: " + key3, ex.getMessage());
    }

    @Test
    void createInterceptorsThrowsWhenInterceptorListHasDuplicateKeys() {
        RequestInterceptorFeature feature1 = mock(RequestInterceptorFeature.class);
        String key1 = "key1";
        when(feature1.key()).thenReturn(key1);
        ClientHttpRequestInterceptor interceptor1 = mock(ClientHttpRequestInterceptor.class);
        when(feature1.interceptor()).thenReturn(interceptor1);

        RequestInterceptorFeature feature2 = mock(RequestInterceptorFeature.class);
        String key2 = "key2";
        when(feature2.key()).thenReturn(key2);
        ClientHttpRequestInterceptor interceptor2 = mock(ClientHttpRequestInterceptor.class);
        when(feature2.interceptor()).thenReturn(interceptor2);


        List<RequestInterceptorFeature> features = List.of(feature2, feature1);
        RequestInterceptorRegistry registry = new RequestInterceptorRegistry(features);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> registry.createInterceptors(List.of(key1, key2, key2)));

        assertEquals("Duplicate request interceptor for key: " + key2, ex.getMessage());
    }

    @Test
    void constructorThrowsExceptionWhenFeaturesHaveDuplicateKeys() {
        RequestInterceptorFeature feature1 = mock(RequestInterceptorFeature.class);
        String key = "key";
        when(feature1.key()).thenReturn(key);
        ClientHttpRequestInterceptor interceptor1 = mock(ClientHttpRequestInterceptor.class);
        when(feature1.interceptor()).thenReturn(interceptor1);

        RequestInterceptorFeature feature2 = mock(RequestInterceptorFeature.class);

        when(feature2.key()).thenReturn(key);
        ClientHttpRequestInterceptor interceptor2 = mock(ClientHttpRequestInterceptor.class);
        when(feature2.interceptor()).thenReturn(interceptor2);

        List<RequestInterceptorFeature> features = List.of(feature2, feature1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new RequestInterceptorRegistry(features));

        assertEquals("Request interceptor features share duplicate key: " + key, ex.getMessage());
    }

}