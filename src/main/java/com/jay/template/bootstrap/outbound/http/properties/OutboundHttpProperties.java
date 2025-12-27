package com.jay.template.bootstrap.outbound.http.properties;

import java.time.Duration;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "platform.outbound.http")
@Validated
public record OutboundHttpProperties(
        @NotNull @Valid ClientDefaults clientDefaults,
        @NotEmpty @Valid Map<String, ClientConfig> clients
) {

    /*
     * Per client config related
     */

    public record ClientDefaults(
            @NotNull Duration connectTimeout,
            @NotNull Duration readTimeout,
            // Only defaults cascade @Validation; ClientConfig resiliency may override or use defaults
            @NotNull @Valid Resiliency resiliency
    ) {}

    public record ClientConfig(
            @NotNull String baseUrl,
            Duration connectTimeout,
            Duration readTimeout,
            Resiliency resiliency
    ) {
        public Duration connectTimeoutOrDefault(ClientDefaults defaults) {
            return connectTimeout == null ? defaults.connectTimeout() : connectTimeout;
        }

        public Duration readTimeoutOrDefault(ClientDefaults defaults) {
            return readTimeout == null ? defaults.readTimeout() : readTimeout;
        }

        public Resiliency resiliencyOrDefault(ClientDefaults defaults) {
            return resiliency == null ? defaults.resiliency() : resiliency;
        }
    }

    /*
     * Per client resiliency config related
     */

    public record Resiliency(
            @NotNull @Valid Bulkhead bulkhead
    ) {
        public record Bulkhead(
                @NotNull Boolean enabled,
                @NotNull Integer maxConcurrentCalls,
                @NotNull Duration maxWaitDuration
        ) {
            public Boolean enabledOrDefault(Bulkhead defaults) {
                return enabled == null ? defaults.enabled() : enabled;
            }

            public Integer maxConcurrentCallsOrDefault(Bulkhead defaults) {
                return maxConcurrentCalls == null ? defaults.maxConcurrentCalls() : maxConcurrentCalls;
            }

            public Duration maxWaitDurationOrDefault(Bulkhead defaults) {
                return maxWaitDuration == null ? defaults.maxWaitDuration() : maxWaitDuration;
            }
        }

        public Bulkhead bulkheadOrDefault(Resiliency defaults) {
            return bulkhead == null ? defaults.bulkhead() : bulkhead;
        }
    }
}
