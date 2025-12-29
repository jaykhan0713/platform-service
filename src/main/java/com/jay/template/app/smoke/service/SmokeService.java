package com.jay.template.app.smoke.service;

import com.jay.template.core.domain.smoke.SmokeCheckResult;
import com.jay.template.core.port.dependency.ping.PingDependency;
import org.springframework.stereotype.Service;

import com.jay.template.core.domain.dependency.ping.PingResult;

@Service
public class SmokeService {
    private final PingDependency pingDependency;

    public SmokeService(PingDependency pingDependency) {
        this.pingDependency = pingDependency;
    }

    public SmokeCheckResult executeFlow() {
        PingResult pingResult = pingDependency.ping();

        //map ping + other dependencies to business use-case (smoke check in this case)

        String aggregatedMsg = "ping.ok: " + pingResult.ok() + " ping.msg: " + pingResult.msg();
        return new SmokeCheckResult(aggregatedMsg);

    }
}
