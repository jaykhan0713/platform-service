package com.jay.template.bootstrap.outbound.http.client.binding;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.jay.template.bootstrap.outbound.http.properties.OutboundHttpProperties;
import com.jay.template.bootstrap.outbound.resiliency.properties.ResiliencyProperties;
import com.jay.template.core.outbound.http.client.settings.HttpClientSettings;
import com.jay.template.core.outbound.resiliency.policy.ResiliencyPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertiesHttpClientSettingsResolverTest {

    @Test
    void resolvesDefaultsAndOverridesAndReturnsImmutableList() {

        // Defaults
        var defaultBulkhead = new ResiliencyProperties.Bulkhead(
                true,
                10,
                Duration.ZERO
        );

        var defaultCircuitBreaker = new ResiliencyProperties.CircuitBreaker(
                true,
                50,
                Duration.ofMillis(200),
                75,
                ResiliencyProperties.CircuitBreaker.SlidingWindowType.COUNT_BASED,
                100,
                20,
                5,
                Duration.ofSeconds(10)
        );

        var resiliencyDefaults = new ResiliencyProperties(defaultBulkhead, defaultCircuitBreaker);

        var clientDefaults = new OutboundHttpProperties.ClientDefaults(
                Duration.ofSeconds(2),
                Duration.ofSeconds(3),
                resiliencyDefaults
        );

        // Client A uses only defaults (null overrides)
        var clientA = new OutboundHttpProperties.Client(
                "https://a.example.com",
                null,
                null,
                null
        );

        // Client B overrides some settings and some resiliency fields, leaving others null to use defaults
        var overrideBulkhead = new ResiliencyProperties.Bulkhead(
                null,           // use default enabled
                25,             // override maxConcurrentCalls
                null            // use default maxWaitDuration
        );

        var overrideCircuitBreaker = new ResiliencyProperties.CircuitBreaker(
                null,                                         // use default enabled
                60,                                           // override failureRateThreshold
                null,                                         // use default slowCallDurationThreshold
                null,                                         // use default slowCallRateThreshold
                ResiliencyProperties.CircuitBreaker.SlidingWindowType.TIME_BASED, // override type
                null,                                         // use default size
                null,                                         // use default minimum calls
                null,                                         // use default half-open permits
                Duration.ofSeconds(30)                        // override waitDurationInOpenState
        );

        var resiliencyOverride = new ResiliencyProperties(overrideBulkhead, overrideCircuitBreaker);

        var clientB = new OutboundHttpProperties.Client(
                "https://b.example.com",
                Duration.ofSeconds(5),
                null, // use default readTimeout
                resiliencyOverride
        );

        Map<String, OutboundHttpProperties.Client> clients = new LinkedHashMap<>();
        clients.put("clientA", clientA);
        clients.put("clientB", clientB);

        var props = new OutboundHttpProperties(clientDefaults, clients);

        // Execute
        var resolver = new PropertiesHttpClientSettingsResolver(props);
        List<HttpClientSettings> resolved = resolver.provide();

        // Immutable list
        assertThrows(UnsupportedOperationException.class, () -> resolved.add(resolved.get(0)));

        assertEquals(2, resolved.size());

        // Client A assertions (defaults)
        HttpClientSettings a = resolved.get(0);
        assertEquals("clientA", a.clientName());
        assertEquals("https://a.example.com", a.baseUrl());
        assertEquals(Duration.ofSeconds(2), a.connectTimeout());
        assertEquals(Duration.ofSeconds(3), a.readTimeout());

        ResiliencyPolicy aPolicy = a.resiliencyPolicy();
        assertEquals(true, aPolicy.bulkheadPolicy().enabled());
        assertEquals(10, aPolicy.bulkheadPolicy().maxConcurrentCalls());
        assertEquals(Duration.ZERO, aPolicy.bulkheadPolicy().maxWaitDuration());

        assertEquals(true, aPolicy.circuitBreakerPolicy().enabled());
        assertEquals(50, aPolicy.circuitBreakerPolicy().failureRateThreshold());
        assertEquals(Duration.ofMillis(200), aPolicy.circuitBreakerPolicy().slowCallDurationThreshold());
        assertEquals(75, aPolicy.circuitBreakerPolicy().slowCallRateThreshold());
        assertEquals(ResiliencyPolicy.CircuitBreakerPolicy.SlidingWindowType.COUNT_BASED,
                aPolicy.circuitBreakerPolicy().slidingWindowType());
        assertEquals(100, aPolicy.circuitBreakerPolicy().slidingWindowSize());
        assertEquals(20, aPolicy.circuitBreakerPolicy().minimumNumberOfCalls());
        assertEquals(5, aPolicy.circuitBreakerPolicy().permittedNumberOfCallsInHalfOpenState());
        assertEquals(Duration.ofSeconds(10), aPolicy.circuitBreakerPolicy().waitDurationInOpenState());

        // Client B assertions (mix of overrides and defaults)
        HttpClientSettings b = resolved.get(1);
        assertEquals("clientB", b.clientName());
        assertEquals("https://b.example.com", b.baseUrl());
        assertEquals(Duration.ofSeconds(5), b.connectTimeout());
        assertEquals(Duration.ofSeconds(3), b.readTimeout()); // default

        ResiliencyPolicy bPolicy = b.resiliencyPolicy();

        // Bulkhead: enabled default, maxConcurrentCalls override, maxWaitDuration default
        assertEquals(true, bPolicy.bulkheadPolicy().enabled());
        assertEquals(25, bPolicy.bulkheadPolicy().maxConcurrentCalls());
        assertEquals(Duration.ZERO, bPolicy.bulkheadPolicy().maxWaitDuration());

        // Circuit breaker: enabled default, failureRateThreshold override, type override, waitDuration override
        assertEquals(true, bPolicy.circuitBreakerPolicy().enabled());
        assertEquals(60, bPolicy.circuitBreakerPolicy().failureRateThreshold());
        assertEquals(Duration.ofMillis(200), bPolicy.circuitBreakerPolicy().slowCallDurationThreshold()); // default
        assertEquals(75, bPolicy.circuitBreakerPolicy().slowCallRateThreshold()); // default
        assertEquals(ResiliencyPolicy.CircuitBreakerPolicy.SlidingWindowType.TIME_BASED,
                bPolicy.circuitBreakerPolicy().slidingWindowType());
        assertEquals(100, bPolicy.circuitBreakerPolicy().slidingWindowSize()); // default
        assertEquals(20, bPolicy.circuitBreakerPolicy().minimumNumberOfCalls()); // default
        assertEquals(5, bPolicy.circuitBreakerPolicy().permittedNumberOfCallsInHalfOpenState()); // default
        assertEquals(Duration.ofSeconds(30), bPolicy.circuitBreakerPolicy().waitDurationInOpenState());
    }
}
