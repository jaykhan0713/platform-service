package com.jay.template.infra.concurrent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class ContextAwareVirtualThreadFactoryTest {

    @Test
    void newThreadIsVirtual() {
        final ThreadFactory factory = new ContextAwareVirtualThreadFactory(Collections.emptyList());
        Thread thread = factory.newThread(() -> {});
        assertTrue(thread.isVirtual());
    }

    @Test
    void newThreadSeesParentMdc() throws InterruptedException {

        final ThreadFactory factory = new ContextAwareVirtualThreadFactory(List.of(new MdcContextPropagator()));

        MDC.put("mdc-key", "parent");
        AtomicReference<String> inside = new AtomicReference<>();

        Thread thread = factory.newThread(() -> inside.set(MDC.get("mdc-key")));
        thread.start();
        thread.join();

        assertEquals("parent", inside.get());

        MDC.clear();
    }

    @Test
    void newThreadDoesNotPolluteParentMdc() throws InterruptedException {
        final ThreadFactory factory = new ContextAwareVirtualThreadFactory(List.of(new MdcContextPropagator()));

        MDC.put("mdc-key", "parent");

        Thread thread = factory.newThread(() -> MDC.put("mdc-key", "child"));
        thread.start();
        thread.join();

        assertEquals("parent", MDC.get("mdc-key"));

        MDC.clear();
    }
}