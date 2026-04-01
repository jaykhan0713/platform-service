package com.jay.template.bootstrap.concurrent.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jay.template.infra.concurrent.propagation.ContextPropagator;
import com.jay.template.infra.concurrent.propagation.identity.IdentityContextPropagator;

@Configuration
public class ContextPropagationConfiguration {

    @Bean
    public ContextPropagator identityContextPropagator() {
        return new IdentityContextPropagator();
    }

}
