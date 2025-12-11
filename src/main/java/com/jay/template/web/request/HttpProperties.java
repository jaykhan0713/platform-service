package com.jay.template.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.http")
@Validated
public record HttpProperties(@Valid Headers headers,
                             @NotBlank String kind) {
    public record Headers(@NotBlank String userId,
                          @NotBlank String requestId) {}
}
