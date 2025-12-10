package com.jay.template.infra.concurrent;

import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
final class MdcContextPropagator implements ContextPropagator {

    MdcContextPropagator() {}

    @Override
    public Runnable propagate(Runnable task) {
        Map<String, String> captured = MDC.getCopyOfContextMap(); // Calling thread's MDC
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            try {
                if (captured != null) {
                    MDC.setContextMap(captured);
                } else {
                    MDC.clear();
                }
                task.run();
            } finally {
                if (previous != null) {
                    MDC.setContextMap(previous);
                } else {
                    MDC.clear();
                }
            }
        };
    }

    @Override
    public <T> Callable<T> propagate(Callable<T> task) {
        Map<String, String> captured = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();

            try {
                if (captured != null) {
                    MDC.setContextMap(captured);
                } else {
                    MDC.clear();
                }
                return task.call();
            } finally {
                if (previous != null) {
                    MDC.setContextMap(previous);
                } else {
                    MDC.clear();
                }
            }
        };
    }
}
