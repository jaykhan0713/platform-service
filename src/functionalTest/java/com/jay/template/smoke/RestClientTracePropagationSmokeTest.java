package com.jay.template.smoke;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.jay.template.Starter;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = Starter.class, webEnvironment = RANDOM_PORT)
public class RestClientTracePropagationSmokeTest {


    public RestClientTracePropagationSmokeTest() {

    }

    @Test
    void restClientPropagatesTracing() {

    }
}
