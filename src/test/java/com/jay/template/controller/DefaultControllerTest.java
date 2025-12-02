package com.jay.template.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DefaultControllerTest {
    @Test
    void testDefaultEndpoint() {
        DefaultController controller = new DefaultController();
        DefaultController.DefaultResponse defaultResponse = controller.get();
        assertTrue(defaultResponse.message().contains("This works"));
    }
}