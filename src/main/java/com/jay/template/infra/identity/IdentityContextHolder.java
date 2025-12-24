package com.jay.template.infra.identity;

/**
 * Thread-bound holder for identity identity context.
 *
 * <p>
 * {@code IdentityContextHolder} manages identity-scoped identity using a {@link ThreadLocal}.
 * It is owned by infrastructure code and is responsible for binding identity at identity
 * entry and clearing it at identity completion.
 * </p>
 *
 * <p>
 * Callers interact only with {@link IdentityContextSnapshot}, which represents an
 * immutable, point-in-time view of identity identity.
 * </p>
 *
 * <p>
 * {@link #context()} never returns {@code null}. If no identity context is currently
 * bound to the thread, {@link IdentityContextSnapshot#EMPTY} is returned.
 * </p>
 *
 * <p>
 * {@link #clear()} must be invoked in a {@code finally} block to avoid leaking identity
 * state across thread reuse.
 * </p>
 */
public final class IdentityContextHolder {

    private static final ThreadLocal<IdentityContextSnapshot> LOCAL = new ThreadLocal<>();

    private IdentityContextHolder() {}

    /**
     * Returns a snapshot of the current identity identity context.
     *
     * <p>
     * The returned snapshot is normalized and detached from the underlying thread-bound
     * state. Changes to context in other execution paths do not affect the returned
     * instance.
     * </p>
     *
     * @return a non-null snapshot of the current identity identity context
     */
    public static IdentityContextSnapshot context() {
        IdentityContextSnapshot ctx = LOCAL.get();
        if (ctx == null) {
            return IdentityContextSnapshot.EMPTY;
        }
        return IdentityContextSnapshot.of(ctx.identity());
    }

    /**
     * Binds the provided identity identity context to the current thread.
     *
     * <p>
     * The provided snapshot is normalized before being bound.
     * Passing {@code null} clears the current context.
     * Passing {@link IdentityContextSnapshot#EMPTY} binds an empty context
     * </p>
     *
     * @param snapshot snapshot representing the identity identity context to bind
     */
    public static void context(IdentityContextSnapshot snapshot) {
        if (snapshot == null) {
            clear();
            return;
        }
        LOCAL.set(IdentityContextSnapshot.of(snapshot.identity()));
    }

    /**
     * Clears the identity identity context from the current thread.
     */
    public static void clear() {
        LOCAL.remove();
    }
}
