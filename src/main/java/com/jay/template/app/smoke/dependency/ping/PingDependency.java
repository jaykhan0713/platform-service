package com.jay.template.app.smoke.dependency.ping;

import org.springframework.stereotype.Component;

import com.jay.template.app.smoke.dependency.ping.contract.DownstreamPingResponse;
import com.jay.template.infra.outbound.http.client.ping.PingClient;

@Component
public class PingDependency {
    private final PingClient pingClient;

    public PingDependency(PingClient pingClient) {
        this.pingClient = pingClient;
    }

    public DownstreamPingResponse ping() {
        pingClient.ping();
    }
}
