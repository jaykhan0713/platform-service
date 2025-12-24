package com.jay.template.infra.outbound.error;

public enum Reason {
    //Resiliency related
    CIRCUIT_BREAKER_REJECT,
    BULKHEAD_FULL

    //TODO: IO related
}
