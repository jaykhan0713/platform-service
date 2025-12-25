package com.jay.template.bootstrap.outbound.http.properties;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "platform.outbound.http")
@Validated
public record OutboundHttpProperties(
        @NotNull @Valid ClientDefaults defaults,
        @NotEmpty @Valid Map<String, ClientConfig> clients
) {
    public record ClientDefaults(
            @NotNull Duration connectTimeout,
            @NotNull Duration readTimeout,
            @NotNull List<String> requestInterceptors,
            // Only defaults cascade @Validation; client overrides are intentionally partial.
            @NotNull @Valid OutboundHttpClientResiliencyConfig resiliency
    ) {}

    public record ClientConfig(
            @NotNull String baseUrl,
            Duration connectTimeout,
            Duration readTimeout,
            List<String> requestInterceptors,
            OutboundHttpClientResiliencyConfig resiliency
    ) {
        public Duration connectTimeoutOrDefault(ClientDefaults defaults) {
            return connectTimeout == null ? defaults.connectTimeout() : connectTimeout;
        }

        public Duration readTimeoutOrDefault(ClientDefaults defaults) {
            return readTimeout == null ? defaults.readTimeout() : readTimeout;
        }

        public List<String> requestInterceptorsOrDefault(ClientDefaults defaults) {
            return requestInterceptors == null ? defaults.requestInterceptors() : requestInterceptors;
        }

        public OutboundHttpClientResiliencyConfig resiliencyOrDefault(ClientDefaults defaults) {
            return resiliency == null ? defaults.resiliency() : resiliency;
        }
    }
}
