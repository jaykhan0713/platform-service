package com.jay.template.core.outbound.http.client.resolver;

public interface OutboundHttpClientConfigResolver {
    ResolvedHttpClientConfig resolve(String clientName);
}
