package com.jay.template.web.mvc.controller.smoke;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jay.template.app.smoke.service.SmokeService;
import com.jay.template.core.domain.smoke.SmokeCheckResult;
import com.jay.template.web.mvc.controller.smoke.api.model.SmokeResponse;

@Profile("smoke")
@RestController
@Hidden
public class SmokeController {

    private final SmokeService smokeService;

    public SmokeController(SmokeService smokeService) {
        this.smokeService = smokeService;
    }

    @GetMapping("/api/smoke")
    public SmokeResponse get() {
        SmokeCheckResult result = smokeService.runSmokeCheck();

        //map app orchestration model to response DTO
        return new SmokeResponse(result.aggregatedMsg());
    }
}
