package com.jay.template.web.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DefaultControllerTest {
    @Test
    void testDefaultEndpoint() {
        DefaultController controller = new DefaultController();
        DefaultController.DefaultResponse defaultResponse = controller.get();
        assertTrue(defaultResponse.message().contains("12345"));
    }
}