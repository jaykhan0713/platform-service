package com.jay.template.web.controller.smoke;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jay.template.app.smoke.ping.contract.inbound.PingResponse;
import com.jay.template.app.smoke.ping.model.PingModel;
import com.jay.template.app.smoke.ping.service.PingService;

@Profile("smoke")
@RestController
@Hidden
public class PingController {

    private final PingService pingService;

    public PingController(PingService pingService) {
        this.pingService = pingService;
    }

    @GetMapping("/smoke/ping")
    public PingResponse get() {
        PingModel model = pingService.pingDependency();
        return new PingResponse(model.ok(), model.msg());
    }
}
