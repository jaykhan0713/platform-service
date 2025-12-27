package com.jay.template.bootstrap.outbound.http.client.binding;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Component;

import com.jay.template.bootstrap.outbound.http.properties.OutboundHttpProperties;
import com.jay.template.core.port.outbound.http.client.HttpClientConfigRegistry;
import com.jay.template.core.outbound.http.client.config.HttpClientConfig;
import com.jay.template.core.outbound.http.client.config.HttpClientResiliencyConfig;

@Component
public class PropertiesHttpClientConfigRegistry implements HttpClientConfigRegistry {

    private final Map<String, HttpClientConfig> resolvedClientConfigMap;

    public PropertiesHttpClientConfigRegistry(
            OutboundHttpProperties props
    ) {
        var propsClientConfigsMap = props.clients();

        var propsClientConfigDefaults = props.clientDefaults();

        Map<String, HttpClientConfig> builderMap = new HashMap<>(propsClientConfigsMap.size());

        propsClientConfigsMap.forEach((propsClientName, propsClientConfig) -> {

            //resiliency config related mappings
            var propsResiliencyDefaults = propsClientConfigDefaults.resiliency();
            var propsResiliency = propsClientConfig.resiliencyOrDefault(propsClientConfigDefaults);

            //bulkhead
            var bulkheadConfig = mapBulkheadConfig(propsResiliency, propsResiliencyDefaults);

            //TODO: CircuitBreaker..

            var resiliencyConfig =
                    new HttpClientResiliencyConfig(bulkheadConfig);

            HttpClientConfig resolvedClientConfig =
                    new HttpClientConfig(
                            propsClientName,
                            propsClientConfig.baseUrl(),
                            propsClientConfig.connectTimeoutOrDefault(propsClientConfigDefaults),
                            propsClientConfig.readTimeoutOrDefault(propsClientConfigDefaults),
                            resiliencyConfig
                    );

            builderMap.put(propsClientName, resolvedClientConfig);
        });

        this.resolvedClientConfigMap = Map.copyOf(builderMap);
    }

    @Override
    public HttpClientConfig httpClientConfig(String clientName) {
        HttpClientConfig config = resolvedClientConfigMap.get(clientName);
        if (config == null) {
            throw new NoSuchElementException(
                    "No outbound HTTP client configuration found for client name: " + clientName
            );
        }
        return config;
    }

    private HttpClientResiliencyConfig.BulkheadConfig mapBulkheadConfig(
            OutboundHttpProperties.Resiliency propsResiliency,
            OutboundHttpProperties.Resiliency propsResiliencyDefaults
    ) {
        var propsBulkhead = propsResiliency.bulkheadOrDefault(propsResiliencyDefaults);
        var propsBulkheadDefaults = propsResiliencyDefaults.bulkhead();

        return new HttpClientResiliencyConfig.BulkheadConfig(
                propsBulkhead.enabledOrDefault(propsBulkheadDefaults),
                propsBulkhead.maxConcurrentCallsOrDefault(propsBulkheadDefaults),
                propsBulkhead.maxWaitDurationOrDefault(propsBulkheadDefaults)
        );
    }

}
