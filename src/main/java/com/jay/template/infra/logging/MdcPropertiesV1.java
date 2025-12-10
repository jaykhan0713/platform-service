package com.jay.template.infra.logging;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app1.logging.mdc")
@Validated
public class MdcPropertiesV1 {

    @NotEmpty
    private Map<@NotBlank String, @NotBlank String> headers;

    @NotBlank
    private String method;

    @NotBlank
    private String path;

    @NotBlank
    private String status;

    @NotBlank
    private String durationMs;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(String durationMs) {
        this.durationMs = durationMs;
    }
}
