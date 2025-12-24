package com.jay.template.infra.outbound.http.client.interceptor;

import java.util.*;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;

@Component
final public class RequestInterceptorRegistry {
    private final Map<String, ClientHttpRequestInterceptor> interceptorsByFeatureKeyMap;

    //OutboundRequestInterceptorFeature makes sure only app interceptors are injected, and not Spring interceptors
    public RequestInterceptorRegistry(List<RequestInterceptorFeature> features) {
        interceptorsByFeatureKeyMap = new HashMap<>(features.size());
        for (RequestInterceptorFeature feature : features) {
            if (interceptorsByFeatureKeyMap.containsKey(feature.key())) {
                throw new IllegalArgumentException(
                        "Request interceptor features share duplicate key: " + feature.key()
                );
            }

            interceptorsByFeatureKeyMap.put(feature.key(), feature.interceptor());
        }
    }

    public List<ClientHttpRequestInterceptor> createInterceptors(List<String> requestInterceptorKeys) {
        Set<String> seenKeys = new HashSet<>(requestInterceptorKeys.size());

        List<ClientHttpRequestInterceptor> httpRequestInterceptors = new ArrayList<>(requestInterceptorKeys.size());
        for (String key: requestInterceptorKeys) {
            if (seenKeys.contains(key)) {
                throw new IllegalArgumentException("Duplicate request interceptor for key: " + key);
            }

            ClientHttpRequestInterceptor httpRequestInterceptor = interceptorsByFeatureKeyMap.get(key);

            if (httpRequestInterceptor == null) {
                throw new IllegalArgumentException("No request interceptor found for key: " + key);
            }

            httpRequestInterceptors.add(httpRequestInterceptor);
            seenKeys.add(key);
        }
        return httpRequestInterceptors;
    }
}
