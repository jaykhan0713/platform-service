package com.jay.template.infra.concurrent;

import java.util.concurrent.Callable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jay.template.infra.request.Identity;
import com.jay.template.infra.request.IdentityContextHolder;
import com.jay.template.infra.request.IdentityContextSnapshot;

import static org.junit.jupiter.api.Assertions.*;

class IdentityContextPropagatorTest {

    private static final String PARENT_USER = "parent-user";
    private static final String PARENT_REQUEST = "parent-request";
    private static final String CHILD_USER = "child-user";
    private static final String CHILD_REQUEST = "child-request";
    private static final String PREVIOUS_USER = "previous-user";
    private static final String PREVIOUS_REQUEST = "previous-request";

    private final IdentityContextPropagator propagator = new IdentityContextPropagator();

    @BeforeEach
    void setUp() {
        IdentityContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        IdentityContextHolder.clear();
    }

    @Test
    void runnablePropagateAppliesCurrentToCaptured() {
        IdentityContextHolder.setContext(
                IdentityContextSnapshot.of(new Identity(PARENT_USER, PARENT_REQUEST)));
        IdentityContextSnapshot captured = IdentityContextHolder.getContext();

        Runnable propagated = propagator.propagate(() -> {
            IdentityContextSnapshot inside = IdentityContextHolder.getContext();
            assertEquals(captured, inside);
            IdentityContextHolder.setContext(
                    IdentityContextSnapshot.of(new Identity(CHILD_USER, CHILD_REQUEST)));
        });

        propagated.run();

        assertEquals(captured, IdentityContextHolder.getContext());
    }

    @Test
    void runnablePropagateCleansUp() {
        Runnable propagated = propagator.propagate(() ->
            IdentityContextHolder.setContext(
                    IdentityContextSnapshot.of(new Identity(CHILD_USER, CHILD_REQUEST)))
        );

        propagated.run();

        assertSame(IdentityContextSnapshot.EMPTY, IdentityContextHolder.getContext());
    }

    @Test
    void runnablePropagateRestoresPreviousWhenCapturedIsEmpty() {

        Runnable propagated = propagator.propagate(() -> {
            assertSame(IdentityContextSnapshot.EMPTY, IdentityContextHolder.getContext()); // Captured context is empty
        });

        // Between propagate() and run(), something sets new snapshot:
        IdentityContextSnapshot previous = IdentityContextSnapshot.of(new Identity(PREVIOUS_USER, PREVIOUS_REQUEST));
        IdentityContextHolder.setContext(previous);

        propagated.run();

        assertEquals(previous, IdentityContextHolder.getContext());
    }

    @Test
    void callablePropagateAppliesCurrentToCaptured() throws Exception {
        IdentityContextHolder.setContext(
                IdentityContextSnapshot.of(new Identity(PARENT_USER, PARENT_REQUEST)));
        IdentityContextSnapshot captured = IdentityContextHolder.getContext();

        Callable<Identity> propagated = propagator.propagate(() -> {
            IdentityContextSnapshot inside = IdentityContextHolder.getContext();
            Identity id = inside.identity();
            assertEquals(captured, inside);
            IdentityContextHolder.setContext(
                    IdentityContextSnapshot.of(new Identity(CHILD_USER, CHILD_REQUEST)));
            return id;
        });

        Identity result = propagated.call();

        assertEquals(captured.identity(), result);
        assertEquals(captured, IdentityContextHolder.getContext());
    }

    @Test
    void callablePropagateCleansUp() throws Exception {
        Callable<Void> propagated = propagator.propagate(() -> {
                IdentityContextHolder.setContext(
                        IdentityContextSnapshot.of(new Identity(CHILD_USER, CHILD_REQUEST)));
                return null;
            }
        );

        propagated.call();

        assertSame(IdentityContextSnapshot.EMPTY, IdentityContextHolder.getContext());
    }

    @Test
    void callablePropagateRestoresPreviousWhenCapturedIsEmpty() throws Exception {

        Callable<Void> propagated = propagator.propagate(() -> {
            assertSame(IdentityContextSnapshot.EMPTY, IdentityContextHolder.getContext()); // Captured context is empty
            return null;
        });

        // Between propagate() and run(), something sets new snapshot:
        IdentityContextSnapshot previous = IdentityContextSnapshot.of(new Identity(PREVIOUS_USER, PREVIOUS_REQUEST));
        IdentityContextHolder.setContext(previous);

        propagated.call();

        assertEquals(previous, IdentityContextHolder.getContext());
    }
}