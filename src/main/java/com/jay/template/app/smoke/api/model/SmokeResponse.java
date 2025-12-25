package com.jay.template.app.smoke.api.model;

// Dummy response contract for inbound request, would use openapi or other contract api in real world
public record SmokeResponse(boolean ok, String msg) {}
