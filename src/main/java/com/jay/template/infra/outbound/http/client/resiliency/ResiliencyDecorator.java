package com.jay.template.infra.outbound.http.client.resiliency;

import java.time.Duration;

import com.jay.template.infra.outbound.http.client.properties.ClientResiliencyConfig;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;

@Component
public class ResiliencyDecorator {

    private static final String INSTANCE_SUFFIX = "OutboundClient";

    private final BulkheadRegistry bulkheadRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public ResiliencyDecorator(
            BulkheadRegistry bulkheadRegistry,
            CircuitBreakerRegistry circuitBreakerRegistry
    ) {
        this.bulkheadRegistry = bulkheadRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    public ClientHttpRequestFactory decorate(
            ClientHttpRequestFactory delegate,
            ClientResiliencyConfig resiliencyConfig,
            ClientResiliencyConfig defaults,
            String clientName
    ) {
        String instanceName = createInstanceName(clientName);
        ClientHttpRequestFactory decorated = delegate;


        /*
         * Order matters, need to make sure the outermost decoration tries to acquire permit w/ bulkhead
         * before any other resiliency decoration tries anything.
         */
        decorated =
                decorate(delegate, resiliencyConfig.bulkhead(), defaults.bulkhead(), instanceName);

        return decorated;
    }

    //bulkhead
    private ClientHttpRequestFactory decorate(
            ClientHttpRequestFactory delegate,
            ClientResiliencyConfig.BulkheadConfig clientBulkheadConfig,
            ClientResiliencyConfig.BulkheadConfig clientBulkheadDefaults,
            String instanceName
    ) {

        if (clientBulkheadConfig.enabledOrDefault(clientBulkheadDefaults)) {
            Integer maxConcurrentCalls =
                    clientBulkheadConfig.maxConcurrentCallsOrDefault(clientBulkheadDefaults);
            Duration maxWaitDuration =
                    clientBulkheadConfig.maxWaitDurationOrDefault(clientBulkheadDefaults);

            BulkheadConfig.Builder bulkheadConfigBuilder = new BulkheadConfig.Builder();
            bulkheadConfigBuilder.maxConcurrentCalls(maxConcurrentCalls);
            bulkheadConfigBuilder.maxWaitDuration(maxWaitDuration);

            //registry should create new instance
            Bulkhead bulkhead = bulkheadRegistry.bulkhead(instanceName, bulkheadConfigBuilder.build());

            return new BulkheadClientHttpRequestFactory(delegate, bulkhead);

        }

        return delegate;
    }

    //circuit breaker

    private String createInstanceName(String clientName) {
        return clientName + INSTANCE_SUFFIX;
    }
}
