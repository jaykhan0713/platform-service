package com.jay.template.app.smoke.service;

import org.springframework.stereotype.Service;

import com.jay.template.app.smoke.dependency.ping.contract.DownstreamPingResponse;
import com.jay.template.app.smoke.model.SmokeModel;
import com.jay.template.infra.outbound.http.client.ping.PingClient;

@Service
public class SmokeService {
    private final PingClient pingClient;

    public SmokeService(PingClient pingClient) {
        this.pingClient = pingClient;
    }

    public SmokeModel executeFlow() {
        DownstreamPingResponse response = pingClient.ping();

        boolean ok = "pong".equals(response.msg());

        //create Model, do more processing if needed. Return model.

        return new SmokeModel(ok, response.msg());

    }
}
