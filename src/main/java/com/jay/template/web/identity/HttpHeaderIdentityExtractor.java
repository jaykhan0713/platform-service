package com.jay.template.web.identity;

import com.jay.template.infra.identity.RequestIdentity;
import com.jay.template.web.request.HttpProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public final class HttpHeaderIdentityExtractor {
    private final HttpProperties httpProps;

    public HttpHeaderIdentityExtractor(HttpProperties httpProps) {
        this.httpProps = httpProps;
    }

    public RequestIdentity extract(HttpServletRequest request) {
        String userId = request.getHeader(httpProps.headers().userId());
        String requestId = request.getHeader(httpProps.headers().requestId());
        return new RequestIdentity(userId, requestId);
    }
}
