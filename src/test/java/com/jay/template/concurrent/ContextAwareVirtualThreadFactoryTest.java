package com.jay.template.concurrent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class ContextAwareVirtualThreadFactoryTest {

    private  final ContextAwareVirtualThreadFactory factory = new ContextAwareVirtualThreadFactory();

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void newThreadIsVirtual() {
        Thread thread = factory.newThread(() -> {});
        assertTrue(thread.isVirtual());
    }

    @Test
    void newThreadSeesParentMdc() throws InterruptedException {
        MDC.put("mdc-key", "parent");
        AtomicReference<String> inside = new AtomicReference<>();

        Thread thread = factory.newThread(() -> inside.set(MDC.get("mdc-key")));
        thread.start();
        thread.join();

        assertEquals("parent", inside.get());
    }

    @Test
    void newThreadDoesNotPolluteParentMdc() throws InterruptedException {
        MDC.put("mdc-key", "parent");

        Thread thread = factory.newThread(() -> MDC.put("mdc-key", "child"));
        thread.start();
        thread.join();

        assertEquals("parent", MDC.get("mdc-key"));
    }
}