package com.jay.template.smoke;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import com.jay.template.common.FunctionalTestBase;
import com.jay.template.common.SpringBootTestShared;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTestShared
public class ApplicationStartupTest extends FunctionalTestBase {

    private final ApplicationContext appContext;

    public ApplicationStartupTest(ApplicationContext appContext) {
        this.appContext = appContext;
    }

    @Test
    void contextLoads() {
        assertNotNull(appContext);
    }
}
