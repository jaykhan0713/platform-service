package com.jay.template.bootstrap.transport.http.headers.binding;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.jay.template.bootstrap.transport.http.properties.TransportHttpProperties;
import com.jay.template.core.port.transport.http.IdentityHeadersProvider;
import com.jay.template.core.transport.http.IdentityHeaders;

@Component
public class PropertiesIdentityHeadersProvider implements IdentityHeadersProvider {

    private final IdentityHeaders identityHeaders;

    public PropertiesIdentityHeadersProvider(TransportHttpProperties transportHttpProps) {
        var headers = transportHttpProps.http().headers();
        this.identityHeaders =
                new IdentityHeaders(
                        headers.userId().trim().toLowerCase(Locale.ROOT),
                        headers.requestId().trim().toLowerCase(Locale.ROOT)
                );
    }

    @Override
    public IdentityHeaders identityHeaders() {
        return identityHeaders;
    }
}
