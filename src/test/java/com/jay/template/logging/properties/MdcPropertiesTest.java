package com.jay.template.logging.properties;

import static org.junit.jupiter.api.Assertions.*;

import com.jay.template.helper.YamlBinder;
import org.junit.jupiter.api.Test;

class MdcPropertiesTest {
    private static final String PROPS_KEY = "app.logging.mdc";

    @Test
    void propertiesAreLoaded() throws Exception {
        YamlBinder binder = new YamlBinder();
        MdcProperties props = binder.bind(PROPS_KEY, MdcProperties.class);

        assertTrue(props.getHeaders().containsKey("x-gateway-trace-id"));
        assertTrue(props.getHeaders().containsKey("x-user-id"));
        assertEquals("http.status", props.getStatus());
        assertEquals("http.method", props.getMethod());
        assertEquals("http.path", props.getPath());
        assertEquals("http.durationMs", props.getDurationMs());
    }
}