package com.jay.template.infra.http.interceptor;

import org.springframework.http.client.ClientHttpRequestInterceptor;

// Self-Registering strategy
public interface OutboundRequestInterceptorFeature {

    ClientHttpRequestInterceptor interceptor();
    String key();
}
