package com.jay.template.infra.http;

import java.time.Duration;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.outbound.http")
public record OutboundHttpProperties(
        @NotNull @Valid ClientDefaults defaults,
        @NotEmpty @Valid Map<String, ClientConfig> clients
) {
    public record ClientDefaults(
            @NotNull Duration connectTimeout,
            @NotNull Duration readTimeout,
            @NotNull Boolean propagateIdentity,
            @NotNull String acceptEncoding
    ) {}

    public record ClientConfig(
            @NotNull String baseUrl,
            Duration connectTimeout,
            Duration readTimeout,
            Boolean propagateIdentity,
            String acceptEncoding
    ) {
        public Duration connectTimeoutOrDefault(ClientDefaults defaults) {
            return connectTimeout == null ? defaults.connectTimeout() : connectTimeout;
        }

        public Duration readTimeoutOrDefault(ClientDefaults defaults) {
            return readTimeout == null ? defaults.readTimeout() : readTimeout;
        }

        public Boolean propagateIdentityOrDefault(ClientDefaults defaults) {
            return propagateIdentity == null ? defaults.propagateIdentity() : propagateIdentity;
        }

        public String acceptEncodingOrDefault(ClientDefaults defaults) {
            return acceptEncoding == null ? defaults.acceptEncoding : acceptEncoding;
        }
    }
}
