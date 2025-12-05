package com.jay.template.concurrent.mdc;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class MdcContextTest {

    @Test
    void runnableWrapSetsCurrentToCaptured() {
        MDC.put("mdc-key", "parent");
        AtomicReference<String> inside = new AtomicReference<>();

        Runnable wrapped = MdcContext.wrap(() -> {
            inside.set(MDC.get("mdc-key"));
            MDC.put("mdc-key", "child");
        });

        wrapped.run();

        assertEquals("parent", inside.get());
        assertEquals("parent", MDC.get("mdc-key"));
    }

    @Test
    void runnableWrapCleansUp() {

        MDC.clear();
        Runnable wrapped = MdcContext.wrap(() -> {
            MDC.put("mdc-key", "child");
        });

        wrapped.run();

        assertNull(MDC.getCopyOfContextMap());
    }

    @Test
    void runnableWrapRestoresPreviousMdcEvenIfCapturedIsNull() {
        MDC.clear();
        Runnable wrapped = MdcContext.wrap(() -> {
            assertNull(MDC.getCopyOfContextMap());
        });

        // Between wrap() and run(), something sets MDC:
        MDC.put("mdc-key", "previous");

        wrapped.run();

        assertEquals("previous", MDC.get("mdc-key"));
    }

    @Test
    void callableWrapSetsCurrentToCaptured() throws Exception {
        MDC.put("mdc-key", "parent");
        AtomicReference<String> inside = new AtomicReference<>();

        Callable<String> wrapped = MdcContext.wrap(() -> {
            inside.set(MDC.get("mdc-key"));
            MDC.put("mdc-key", "child");
            return "result";
        });

        String result = wrapped.call();

        assertEquals("result", result);
        assertEquals("parent", inside.get());
        assertEquals("parent", MDC.get("mdc-key"));
    }



    @Test
    void callableWrapCleansUp() throws Exception {

        MDC.clear();

        Callable<String> wrapped = MdcContext.wrap(() -> {
            MDC.put("mdc-key", "child");
            return "result";
        });

        String result = wrapped.call();

        assertEquals("result", result);
        assertNull(MDC.getCopyOfContextMap());
    }

    @Test
    void callableWrapRestoresPreviousMdcEvenIfCapturedIsNull() throws Exception {
        MDC.clear();
        Callable<String> wrapped = MdcContext.wrap(() -> {
            assertNull(MDC.getCopyOfContextMap());
            return "result";
        });

        // Between wrap() and run(), something sets MDC:
        MDC.put("mdc-key", "previous");

        String result = wrapped.call();

        assertEquals("result", result);
        assertEquals("previous", MDC.get("mdc-key"));
    }
}