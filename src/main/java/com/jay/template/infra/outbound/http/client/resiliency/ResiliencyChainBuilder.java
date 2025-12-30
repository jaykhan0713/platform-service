package com.jay.template.infra.outbound.http.client.resiliency;

import java.time.Duration;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.http.client.ClientHttpRequestFactory;

import com.jay.template.core.outbound.http.client.config.HttpClientResiliencyConfig;
import com.jay.template.infra.outbound.http.client.resiliency.bulkhead.BulkheadClientHttpRequestFactoryDecorator;

//orchestration of functional resiliency responsibilities
public class ResiliencyChainBuilder {

    //note that micrometer r4j metrics will use same instance name but give it distinct (bulkhead/cb) metrics
    private static final String INSTANCE_SUFFIX = "outbound.client";

    private final BulkheadRegistry bulkheadRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public ResiliencyChainBuilder(
            BulkheadRegistry bulkheadRegistry,
            CircuitBreakerRegistry circuitBreakerRegistry
    ) {
        this.bulkheadRegistry = bulkheadRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    public ClientHttpRequestFactory applyBulkhead(
            ClientHttpRequestFactory delegate,
            HttpClientResiliencyConfig resiliencyConfig,
            String clientName
    ) {
        String instanceName = createInstanceName(clientName);
        ClientHttpRequestFactory decorated = delegate;


        /*
         * Order matters, need to make sure the outermost decoration tries to acquire permit w/ bulkhead
         * before any other resiliency decoration tries anything.
         */
        decorated =
                applyBulkhead(decorated, resiliencyConfig.bulkheadConfig(), instanceName);

        return decorated;
    }

    //bulkhead
    private ClientHttpRequestFactory applyBulkhead(
            ClientHttpRequestFactory delegate,
            HttpClientResiliencyConfig.BulkheadConfig clientBulkheadConfig,
            String instanceName
    ) {

        if (clientBulkheadConfig.enabled()) {
            int maxConcurrentCalls =
                    clientBulkheadConfig.maxConcurrentCalls();
            Duration maxWaitDuration =
                    clientBulkheadConfig.maxWaitDuration();

            BulkheadConfig.Builder bulkheadConfigBuilder = new BulkheadConfig.Builder();
            bulkheadConfigBuilder.maxConcurrentCalls(maxConcurrentCalls);
            bulkheadConfigBuilder.maxWaitDuration(maxWaitDuration);

            //registry should create new instance
            Bulkhead bulkhead = bulkheadRegistry.bulkhead(instanceName, bulkheadConfigBuilder.build());

            return new BulkheadClientHttpRequestFactoryDecorator(delegate, bulkhead);
        }

        return delegate;
    }

    // TODO: circuit breaker

    private String createInstanceName(String clientName) {
        return clientName + INSTANCE_SUFFIX;
    }
}
