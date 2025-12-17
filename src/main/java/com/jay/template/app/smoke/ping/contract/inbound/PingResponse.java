package com.jay.template.app.smoke.ping.contract.inbound;

// Dummy response contract for inbound request, would use openapi or other contract api in real world
public record PingResponse(boolean ok, String msg) {}
