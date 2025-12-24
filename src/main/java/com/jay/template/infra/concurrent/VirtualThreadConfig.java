package com.jay.template.infra.concurrent;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class VirtualThreadConfig {

    @Bean //defensive copy as propagators List is a mutable bean
    ThreadFactory contextAwareVirtualThreadFactory(List<ContextPropagator> propagators) {
        return new ContextAwareVirtualThreadFactory(List.copyOf(propagators));
    }

    @Bean(destroyMethod = "shutdown")
    ExecutorService virtualThreadPerTaskExecutor(ThreadFactory contextAwareVirtualThreadFactory) {
        return Executors.newThreadPerTaskExecutor(contextAwareVirtualThreadFactory);
    }
}
