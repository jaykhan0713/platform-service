package com.jay.template.core.error.dependency;

import java.util.Objects;

// infra layer exception bridge to app.
public class DependencyCallException extends RuntimeException {

    private final Reason reason;
    private final String clientName;

    public DependencyCallException(Reason reason, String clientName, Throwable cause) {
        super(cause);
        this.reason = Objects.requireNonNull(reason);
        this.clientName = Objects.requireNonNull(clientName);
    }

    public Reason reason() {
        return reason;
    }

    public String clientName() {
        return clientName;
    }
}
