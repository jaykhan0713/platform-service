package com.jay.template.infra.logging;

import static org.junit.jupiter.api.Assertions.*;

import com.jay.template.helper.YamlBinder;
import org.junit.jupiter.api.Test;

class MdcPropertiesV1Test {
    private static final String PROPS_KEY = "app.logging.mdc";

    @Test
    void propertiesAreLoaded() throws Exception {
        YamlBinder binder = new YamlBinder();
        MdcPropertiesV1 props = binder.bind(PROPS_KEY, MdcPropertiesV1.class);

        assertTrue(props.getHeaders().containsKey("x-request-id"));
        assertTrue(props.getHeaders().containsKey("x-user-id"));
        assertEquals("http.status", props.getStatus());
        assertEquals("http.method", props.getMethod());
        assertEquals("http.path", props.getPath());
        assertEquals("http.durationMs", props.getDurationMs());
    }
}