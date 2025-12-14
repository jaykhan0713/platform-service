package com.jay.template.infra.request;

/**
 * Immutable identity associated with a request.
 *
 * <p>
 * {@code Identity} represents the caller identity as known to the service.
 * It may originate from an API gateway, reverse proxy, or another service.
 * </p>
 *
 * <p>
 * All fields are non-null. Missing or unknown values are normalized to empty strings
 * during construction.
 * </p>
 *
 * <p>
 * {@code Identity} is immutable and represents stable request identifiers used for
 * correlation, auditing, and logging.
 * </p>
 *
 * @param userId identifier of the calling user or service, if available
 * @param requestId correlation identifier for the request, if available
 */
public record Identity(String userId, String requestId) {

    public Identity {
        userId = normalizeIdentityValue(userId);
        requestId = normalizeIdentityValue(requestId);
    }

    private static String normalizeIdentityValue(String value) {
        return value == null ? "" : value;
    }
}