package com.jay.template.infra.http.client;

import java.util.*;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;

import com.jay.template.infra.http.interceptor.OutboundRequestInterceptorFeature;

@Component
final class RequestInterceptorRegistry {
    private final Map<String, ClientHttpRequestInterceptor> featureMap;

    //OutboundRequestInterceptorFeature makes sure only app interceptors are injected, and not Spring interceptors
    RequestInterceptorRegistry(List<OutboundRequestInterceptorFeature> features){
        featureMap = new HashMap<>(features.size());
        for (OutboundRequestInterceptorFeature feature : features) {
            if (featureMap.containsKey(feature.key())) {
                throw new IllegalArgumentException(
                        "Request interceptor features share duplicate key: " + feature.key()
                );
            }

            featureMap.put(feature.key(), feature.interceptor());
        }
    }

    List<ClientHttpRequestInterceptor> getInterceptors(List<String> requestInterceptorKeys) {
        Set<String> seenKeys = new HashSet<>(requestInterceptorKeys.size());

        List<ClientHttpRequestInterceptor> httpRequestInterceptors = new ArrayList<>(requestInterceptorKeys.size());
        for (String key: requestInterceptorKeys){
            if (seenKeys.contains(key)){
                throw new IllegalArgumentException("Duplicate request interceptor for key: " + key);
            }

            ClientHttpRequestInterceptor httpRequestInterceptor = featureMap.get(key);

            if (httpRequestInterceptor == null){
                throw new IllegalArgumentException("No request interceptor found for key: " + key);
            }

            httpRequestInterceptors.add(httpRequestInterceptor);
            seenKeys.add(key);
        }
        return httpRequestInterceptors;
    }
}
