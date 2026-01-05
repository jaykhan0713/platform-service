package com.jay.template.core.error.dependency;

import java.util.Objects;

// infra layer error bridge to app.
public class DependencyCallException extends RuntimeException {

    private final String clientName;
    private final Reason reason;

    public DependencyCallException(
            String clientName,
            Reason reason
    ) {
        this(clientName, reason, null);
    }

    public DependencyCallException(
            String clientName,
            Reason reason,
            Throwable cause
    ) {
        super(cause);
        this.clientName = Objects.requireNonNull(clientName);
        this.reason = Objects.requireNonNull(reason);
    }

    public String clientName() {
        return clientName;
    }

    public Reason reason() {
        return reason;
    }
}
