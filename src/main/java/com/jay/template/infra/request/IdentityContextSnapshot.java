package com.jay.template.infra.request;

/**
 * Snapshot of request-scoped context data.
 *
 * <p>
 * {@code RequestContextSnapshot} represents a point-in-time view of request context.
 * It is safe to pass across layers and thread boundaries.
 * </p>
 *
 * <p>
 * Snapshots are copied on both retrieval and binding to ensure isolation between
 * execution paths. Identity is reused as-is because it is immutable.
 * </p>
 *
 * <p>
 * To modify request context, a new snapshot must be created and set via
 * {@link IdentityContextHolder#setContext(IdentityContextSnapshot)}.
 * </p>
 *
 * @param identity immutable identity associated with the request
 */
public record IdentityContextSnapshot(Identity identity) {

    /**
     * Represents an empty request context when no identity is available.
     */
    public static final IdentityContextSnapshot EMPTY =
            new IdentityContextSnapshot(new Identity("", ""));
}