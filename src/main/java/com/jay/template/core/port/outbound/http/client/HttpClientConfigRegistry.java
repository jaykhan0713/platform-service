package com.jay.template.core.port.outbound.http.client;

import com.jay.template.core.outbound.http.client.config.HttpClientConfig;

public interface HttpClientConfigRegistry {

    HttpClientConfig httpClientConfig(String clientName);
}
