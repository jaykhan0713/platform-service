package com.jay.template.smoke;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.jay.template.Starter;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Starter.class)
public class ApplicationStartupTest {

    private final ApplicationContext appContext;

    public ApplicationStartupTest(ApplicationContext appContext) {
        this.appContext = appContext;
    }

    @Test
    void contextLoads() {
        assertNotNull(appContext);
    }
}
