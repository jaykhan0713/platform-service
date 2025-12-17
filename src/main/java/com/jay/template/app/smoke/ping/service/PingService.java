package com.jay.template.app.smoke.ping.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.jay.template.app.smoke.ping.contract.outbound.DownstreamPingResponse;
import com.jay.template.app.smoke.ping.model.PingModel;
import com.jay.template.app.smoke.ping.rest.PingClient;

@Service
public class PingService {
    private final PingClient pingClient;

    public PingService(PingClient pingClient) {
        this.pingClient = pingClient;
    }

    public PingModel pingDependency() {
        DownstreamPingResponse response = pingClient.ping();

        //create Model, do more processing if needed. Return model.

        return new PingModel(true, response.msg());

    }
}
