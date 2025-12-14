package com.jay.template.infra.request;

/**
 * Thread-bound holder for request context snapshots.
 *
 * <p>
 * {@code RequestContextHolder} manages request-scoped context using {@link ThreadLocal}.
 * It is owned by infrastructure code and is responsible for binding context at request
 * entry and clearing it at request completion.
 * </p>
 *
 * <p>
 * Callers interact only with {@link IdentityContextSnapshot}, which represents a
 * defensive, point-in-time copy of request context data.
 * </p>
 *
 * <p>
 * {@link #getContext()} never returns {@code null}. If no context is currently bound to
 * the thread, {@link IdentityContextSnapshot#EMPTY} is returned.
 * </p>
 *
 * <p>
 * {@link #clear()} must be invoked in a {@code finally} block to avoid leaking request
 * state across thread reuse.
 * </p>
 */
public final class IdentityContextHolder {

    private static final ThreadLocal<IdentityContextSnapshot> LOCAL = new ThreadLocal<>();

    private IdentityContextHolder() {}

    /**
     * Returns a snapshot of the current request context.
     *
     * <p>
     * The returned snapshot is a defensive copy. Mutations to other snapshots do not
     * affect the returned instance.
     * </p>
     *
     * @return a non-null snapshot of the current request context
     */
    public static IdentityContextSnapshot getContext() {
        IdentityContextSnapshot ctx = LOCAL.get();
        if (ctx == null) {
            ctx = IdentityContextSnapshot.EMPTY;
        }
        return new IdentityContextSnapshot(ctx.identity());
    }

    /**
     * Sets the request context for the current thread.
     *
     * <p>
     * The provided snapshot is defensively copied before being bound to the thread.
     * Passing {@code null} clears the current context.
     * </p>
     *
     * @param ctxSnapshot snapshot representing the request context to bind
     */
    public static void setContext(IdentityContextSnapshot ctxSnapshot) {
        if (ctxSnapshot == null) {
            clear();
            return;
        }
        LOCAL.set(new IdentityContextSnapshot(ctxSnapshot.identity()));
    }

    /**
     * Clears the request context from the current thread.
     */
    public static void clear() {
        LOCAL.remove();
    }
}
