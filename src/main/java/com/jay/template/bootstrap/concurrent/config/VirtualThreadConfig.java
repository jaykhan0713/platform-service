package com.jay.template.bootstrap.concurrent.config;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jay.template.infra.concurrent.PlatformVirtualThreadFactory;
import com.jay.template.infra.concurrent.propagation.ContextPropagator;

@Configuration
public class VirtualThreadConfig {

    @Bean
    ThreadFactory platformVirtualThreadFactory(List<ContextPropagator> propagators) {
        return new PlatformVirtualThreadFactory(List.copyOf(propagators));
    }

    @Bean(name = "platformVirtualThreadExecutor", destroyMethod = "shutdown")
    ExecutorService platformVirtualThreadExecutor(ThreadFactory platformVirtualThreadFactory) {
        return Executors.newThreadPerTaskExecutor(platformVirtualThreadFactory);
    }
}
