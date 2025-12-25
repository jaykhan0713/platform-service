package com.jay.template.bootstrap.outbound.http.client.resolver;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.jay.template.bootstrap.outbound.http.properties.OutboundHttpProperties;
import com.jay.template.core.outbound.http.client.resolver.OutboundHttpClientConfigResolver;
import com.jay.template.core.outbound.http.client.resolver.ResolvedHttpClientConfig;

// Note that boostrap properties
@Component
public class PropertiesOutboundHttpClientConfigResolver implements OutboundHttpClientConfigResolver {

    private final Map<String, ResolvedHttpClientConfig> resolvedClientConfigMap;

    public PropertiesOutboundHttpClientConfigResolver(
            OutboundHttpProperties props
    ) {

        var propClients = props.clients();
        var propClientDefaults = props.defaults();

        Map<String, ResolvedHttpClientConfig> builderMap = new HashMap<>(propClients.size());

        propClients.forEach((propClientName, propClientConfig) -> {

            ResolvedHttpClientConfig resolvedClientConfig =
                    new ResolvedHttpClientConfig(
                            propClientName,
                            propClientConfig.baseUrl(),
                            propClientConfig.connectTimeoutOrDefault(propClientDefaults),
                            propClientConfig.readTimeoutOrDefault(propClientDefaults),
                            propClientConfig.requestInterceptorsOrDefault(propClientDefaults) //next up is ResiliencyConfig
                    );

            builderMap.put(propClientName, resolvedClientConfig);
        });

        this.resolvedClientConfigMap = Map.copyOf(builderMap);
    }

    @Override
    public ResolvedHttpClientConfig resolve(String clientName) {
        ResolvedHttpClientConfig config = resolvedClientConfigMap.get(clientName);
        if (config == null) {
            throw new IllegalArgumentException(
                    "No outbound HTTP client configuration found for client name: " + clientName
            );
        }
        return config;
    }

}
