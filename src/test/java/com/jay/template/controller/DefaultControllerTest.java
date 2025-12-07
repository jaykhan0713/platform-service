package com.jay.template.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.jay.template.logging.mdc.MdcRetriever;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DefaultControllerTest {
    @Test
    void testDefaultEndpoint() {
        MdcRetriever mdcRetriever = Mockito.mock(MdcRetriever.class);
        Mockito.when(mdcRetriever.getUserId()).thenReturn("12345");

        DefaultController controller = new DefaultController(mdcRetriever);
        DefaultController.DefaultResponse defaultResponse = controller.get();
        assertTrue(defaultResponse.message().contains("12345"));
    }
}