package com.jay.template.infra.concurrent;

import java.util.concurrent.Callable;

import com.jay.template.infra.request.IdentityContextHolder;
import com.jay.template.infra.request.IdentityContextSnapshot;

/**
 * Propagates {@link IdentityContextSnapshot} across execution boundaries.
 *
 * <p>
 * {@code IdentityContextPropagator} captures the current {@link IdentityContextSnapshot}
 * at the time {@code propagate} is called and returns a wrapped task that applies the
 * captured context when executed.
 * </p>
 *
 * <p>
 * On execution, the wrapper records the context currently bound to the executing thread
 * and restores it in a {@code finally} block. This ensures the executing thread's prior
 * identity context is not leaked or overwritten after the task completes.
 * </p>
 *
 * <p>
 * The {@link IdentityContextSnapshot#EMPTY} sentinel represents an unbound context.
 * When applying {@code EMPTY}, the propagator clears the underlying {@link ThreadLocal}
 * via {@link IdentityContextHolder#clear()}.
 * </p>
 */
final class IdentityContextPropagator implements ContextPropagator {

    /**
     * Wraps a {@link Runnable} to execute with the identity context captured at wrapping time.
     *
     * <p>
     * The executing thread's previous context is restored after execution, even if the
     * task throws.
     * </p>
     *
     * @param task the task to wrap
     * @return a runnable that applies captured identity context during execution
     */
    @Override
    public Runnable propagate(Runnable task) {
        IdentityContextSnapshot captured = IdentityContextHolder.getContext();
        return () -> {
            IdentityContextSnapshot previous = IdentityContextHolder.getContext();

            try {
                apply(captured);
                task.run();
            } finally {
                apply(previous);
            }
        };
    }

    /**
     * Wraps a {@link Callable} to execute with the identity context captured at wrapping time.
     *
     * <p>
     * The executing thread's previous context is restored after execution, even if the
     * task throws.
     * </p>
     *
     * @param task the task to wrap
     * @param <T> the callable return type
     * @return a callable that applies captured identity context during execution
     */
    @Override
    public <T> Callable<T> propagate(Callable<T> task) {
        IdentityContextSnapshot captured = IdentityContextHolder.getContext();
        return () -> {
            IdentityContextSnapshot previous = IdentityContextHolder.getContext();

            try {
                apply(captured);
                return task.call();
            } finally {
                apply(previous);
            }
        };
    }

    /**
     * Applies the provided snapshot to the current thread.
     *
     * <p>
     * {@link IdentityContextSnapshot#EMPTY} clears the current thread context to preserve
     * sentinel semantics.
     * </p>
     *
     * @param snapshot the snapshot to apply
     */
    private void apply(IdentityContextSnapshot snapshot) {
        if (snapshot == IdentityContextSnapshot.EMPTY) { // sentinel instance check
            IdentityContextHolder.clear();
        } else {
            IdentityContextHolder.setContext(snapshot);
        }
    }
}
