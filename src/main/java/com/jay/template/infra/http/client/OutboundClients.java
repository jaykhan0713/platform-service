package com.jay.template.infra.http.client;

public enum OutboundClients {
    INTERNAL("internal"),
    EXTERNAL("external");

    private final String clientName;

    OutboundClients(String clientName) {
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }
}
