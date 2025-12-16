package com.jay.template.infra.http;

import java.time.Duration;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.outbound.http")
public record OutboundHttpProperties(
        @NotNull @Valid Headers headers,
        @NotNull @Valid ClientDefaults defaults,
        @NotEmpty @Valid Map<String, ClientConfig> clients
) {

    public record Headers(
            @NotNull String userId,
            @NotNull String requestId
    ) {}

    public record ClientDefaults(
            @NotNull Duration connectTimeout,
            @NotNull Duration readTimeout
    ) {}

    public record ClientConfig(
            @NotNull String baseUrl,
            Duration connectTimeout,
            Duration readTimeout,
            @NotNull Boolean propagateIdentity
    ) {
        public Duration connectTimeoutOrDefault(ClientDefaults defaults) {
            return connectTimeout == null ? defaults.connectTimeout() : connectTimeout;
        }

        public Duration readTimeoutOrDefault(ClientDefaults defaults) {
            return readTimeout == null ? defaults.readTimeout() : readTimeout;
        }
    }
}
