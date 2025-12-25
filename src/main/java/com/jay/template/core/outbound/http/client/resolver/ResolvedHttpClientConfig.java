package com.jay.template.core.outbound.http.client.resolver;

import java.time.Duration;
import java.util.List;

import org.springframework.http.client.ClientHttpRequestInterceptor;

public record ResolvedHttpClientConfig(
        String clientName,
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout,
        List<String> requestInterceptors
        //, ResolvedClientResiliencyConfig
) {
}
