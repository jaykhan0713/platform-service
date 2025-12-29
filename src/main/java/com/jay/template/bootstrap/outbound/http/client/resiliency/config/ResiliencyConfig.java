package com.jay.template.bootstrap.outbound.http.client.resiliency.config;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jay.template.infra.outbound.http.client.resiliency.ResiliencyChainBuilder;

@Configuration
public class ResiliencyConfig {

    @Bean
    ResiliencyChainBuilder resiliencyDecorator(
            BulkheadRegistry bulkheadRegistry,
            CircuitBreakerRegistry circuitBreakerRegistry
    ) {
        return new ResiliencyChainBuilder(
                bulkheadRegistry,
                circuitBreakerRegistry
        );
    }
}
