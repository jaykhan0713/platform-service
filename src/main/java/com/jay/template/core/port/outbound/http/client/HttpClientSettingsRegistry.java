package com.jay.template.core.port.outbound.http.client;

import com.jay.template.core.outbound.http.client.settings.HttpClientSettings;

public interface HttpClientSettingsRegistry {

    HttpClientSettings httpClientSettings(String clientName);
}
