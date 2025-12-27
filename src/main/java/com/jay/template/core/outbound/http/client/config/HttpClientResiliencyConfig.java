package com.jay.template.core.outbound.http.client.config;

import java.time.Duration;

public record HttpClientResiliencyConfig(
        BulkheadConfig bulkheadConfig
) {
    public record BulkheadConfig(
            boolean enabled,
            int maxConcurrentCalls,
            Duration maxWaitDuration
    ) {}

    // CircuitBreakerConfig...
}
