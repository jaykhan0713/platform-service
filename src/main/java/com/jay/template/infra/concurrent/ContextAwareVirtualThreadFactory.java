package com.jay.template.infra.concurrent;

import java.util.List;
import java.util.concurrent.ThreadFactory;

final class ContextAwareVirtualThreadFactory implements ThreadFactory {

    private final ThreadFactory delegate;
    private final List<ContextPropagator> propagators;

    public ContextAwareVirtualThreadFactory(List<ContextPropagator> propagators) {
        this.delegate = Thread.ofVirtual().factory();
        this.propagators = propagators;
    }

    @Override
    public Thread newThread(Runnable task) {
        Runnable propogatedTask = task;
        for (ContextPropagator propagator : propagators) {
            propogatedTask = propagator.propagate(propogatedTask);
        }
        return delegate.newThread(propogatedTask);
    }
}
