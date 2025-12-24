package com.jay.template.infra.outbound.http.client.properties;

import java.time.Duration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

//for use with HttpProperties
public record ClientResiliencyConfig(
        @NotNull @Valid BulkheadConfig bulkhead
) {
    public record BulkheadConfig(@NotNull Boolean enabled,
                                 @NotNull Integer maxConcurrentCalls,
                                 @NotNull Duration maxWaitDuration
    ) {
        public Boolean enabledOrDefault(BulkheadConfig defaults) {
            return enabled == null ? defaults.enabled() : enabled;
        }

        public Integer maxConcurrentCallsOrDefault(BulkheadConfig defaults) {
            return maxConcurrentCalls == null ? defaults.maxConcurrentCalls() : maxConcurrentCalls;
        }

        public Duration maxWaitDurationOrDefault(BulkheadConfig defaults) {
            return maxWaitDuration == null ? defaults.maxWaitDuration() : maxWaitDuration;
        }
    }
}
