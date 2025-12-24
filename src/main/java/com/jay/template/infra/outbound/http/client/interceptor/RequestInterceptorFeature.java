package com.jay.template.infra.outbound.http.client.interceptor;

import org.springframework.http.client.ClientHttpRequestInterceptor;

// Self-Registering strategy
public interface RequestInterceptorFeature {

    ClientHttpRequestInterceptor interceptor();

    String key();
}
