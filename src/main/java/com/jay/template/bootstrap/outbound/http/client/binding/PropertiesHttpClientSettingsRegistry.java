package com.jay.template.bootstrap.outbound.http.client.binding;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.jay.template.bootstrap.outbound.resiliency.properties.ResiliencyProperties;
import org.springframework.stereotype.Component;

import com.jay.template.bootstrap.outbound.http.properties.OutboundHttpProperties;
import com.jay.template.core.port.outbound.http.client.HttpClientSettingsRegistry;
import com.jay.template.core.outbound.http.client.settings.HttpClientSettings;
import com.jay.template.core.outbound.resiliency.policy.ResiliencyPolicy;

@Component
public class PropertiesHttpClientSettingsRegistry implements HttpClientSettingsRegistry {

    private final Map<String, HttpClientSettings> resolvedClientSettingsMap;

    public PropertiesHttpClientSettingsRegistry(
            OutboundHttpProperties props
    ) {
        var propsClientMap = props.clients();

        var propsClientDefaults = props.clientDefaults();

        Map<String, HttpClientSettings> builderMap = new HashMap<>(propsClientMap.size());

        propsClientMap.forEach((propsClientName, propsClient) -> {

            //resiliency settings related mappings
            var propsResiliencyDefaults = propsClientDefaults.resiliency();
            var propsResiliency = propsClient.resiliencyOrDefault(propsClientDefaults);

            var bulkheadPolicy = mapBulkheadPolicy(propsResiliency, propsResiliencyDefaults);

            var circuitBreakerPolicy = mapCircuitBreaker(propsResiliency, propsResiliencyDefaults);

            var resiliencyPolicy =
                    new ResiliencyPolicy(bulkheadPolicy, circuitBreakerPolicy);

            HttpClientSettings resolvedClientSettings =
                    new HttpClientSettings(
                            propsClientName,
                            propsClient.baseUrl(),
                            propsClient.connectTimeoutOrDefault(propsClientDefaults),
                            propsClient.readTimeoutOrDefault(propsClientDefaults),
                            resiliencyPolicy
                    );

            builderMap.put(propsClientName, resolvedClientSettings);
        });

        this.resolvedClientSettingsMap = Map.copyOf(builderMap);
    }

    @Override
    public HttpClientSettings httpClientSettings(String clientName) {
        HttpClientSettings settings = resolvedClientSettingsMap.get(clientName);
        if (settings == null) {
            throw new NoSuchElementException(
                    "No outbound HTTP client settings found for client name: " + clientName
            );
        }
        return settings;
    }

    private ResiliencyPolicy.BulkheadPolicy mapBulkheadPolicy(
            ResiliencyProperties propsResiliency,
            ResiliencyProperties propsResiliencyDefaults
    ) {
        var propsBulkhead = propsResiliency.bulkheadOrDefault(propsResiliencyDefaults);
        var propsBulkheadDefaults = propsResiliencyDefaults.bulkhead();

        return new ResiliencyPolicy.BulkheadPolicy(
                propsBulkhead.enabledOrDefault(propsBulkheadDefaults),
                propsBulkhead.maxConcurrentCallsOrDefault(propsBulkheadDefaults),
                propsBulkhead.maxWaitDurationOrDefault(propsBulkheadDefaults)
        );
    }

    private ResiliencyPolicy.CircuitBreakerPolicy mapCircuitBreaker(
            ResiliencyProperties propsResiliency,
            ResiliencyProperties propsResiliencyDefaults
    ) {
        var propsCircuitBreaker = propsResiliency.circuitBreakerOrDefault(propsResiliencyDefaults);
        var propsCircuitBreakerDefaults = propsResiliencyDefaults.circuitBreaker();

        return new ResiliencyPolicy.CircuitBreakerPolicy(
                propsCircuitBreaker.enabledOrDefault(propsCircuitBreakerDefaults),

                propsCircuitBreaker.failureRateThresholdOrDefault(propsCircuitBreakerDefaults),

                propsCircuitBreaker.slowCallDurationThresholdOrDefault(propsCircuitBreakerDefaults),
                propsCircuitBreaker.slowCallRateThresholdOrDefault(propsCircuitBreakerDefaults),

                mapSlidingWindowType(propsCircuitBreaker.slidingWindowTypeOrDefault(propsCircuitBreakerDefaults)),
                propsCircuitBreaker.slidingWindowSizeOrDefault(propsCircuitBreakerDefaults),
                propsCircuitBreaker.minimumNumberOfCallsOrDefault(propsCircuitBreakerDefaults),

                propsCircuitBreaker.permittedNumberOfCallsInHalfOpenStateOrDefault(propsCircuitBreakerDefaults),
                propsCircuitBreaker.waitDurationInOpenStateOrDefault(propsCircuitBreakerDefaults)
        );
    }

    private ResiliencyPolicy.CircuitBreakerPolicy.SlidingWindowType mapSlidingWindowType(
            ResiliencyProperties.CircuitBreaker.SlidingWindowType propsType
    ) {
        return switch (propsType) {
            case COUNT_BASED -> ResiliencyPolicy.CircuitBreakerPolicy.SlidingWindowType.COUNT_BASED;
            case TIME_BASED -> ResiliencyPolicy.CircuitBreakerPolicy.SlidingWindowType.TIME_BASED;
        };
    }

}
