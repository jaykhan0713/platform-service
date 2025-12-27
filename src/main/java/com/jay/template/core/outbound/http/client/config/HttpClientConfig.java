package com.jay.template.core.outbound.http.client.config;

import java.time.Duration;

public record HttpClientConfig(
        String clientName,
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout,
        HttpClientResiliencyConfig resiliencyConfig
) {}
