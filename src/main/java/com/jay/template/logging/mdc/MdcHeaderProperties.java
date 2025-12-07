package com.jay.template.logging.mdc;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.logging.mdc.headers")
public class MdcHeaderProperties {

    @NotBlank
    private String xGatewayTraceId;

    @NotBlank
    private String xUserId;

    public String getxGatewayTraceId() {
        return xGatewayTraceId;
    }

    public void setxGatewayTraceId(String xGatewayTraceId) {
        this.xGatewayTraceId = xGatewayTraceId;
    }

    public String getxUserId() {
        return xUserId;
    }

    public void setxUserId(String xUserId) {
        this.xUserId = xUserId;
    }
}
