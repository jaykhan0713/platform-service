package com.jay.template.infra.concurrent;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class VirtualThreadConfig {

    @Bean
    ThreadFactory contextAwareVirtualThreadFactory(List<ContextPropagator> propagators) {
        return new ContextAwareVirtualThreadFactory(propagators);
    }

    @Bean(destroyMethod = "shutdown")
    ExecutorService virtualThreadPerTaskExecutor(ThreadFactory contextAwareVirtualThreadFactory) {
        return Executors.newThreadPerTaskExecutor(contextAwareVirtualThreadFactory);
    }
}
