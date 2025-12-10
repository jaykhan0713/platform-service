package com.jay.template.infra.logging;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.logging.mdc")
@Validated
public record MdcProperties(
        @NotBlank String userId,
        @NotBlank String requestId,
        @NotBlank String kind,
        @NotBlank String name,
        @NotBlank String method,
        @NotBlank String status,
        @NotBlank String durationMs
) {}
