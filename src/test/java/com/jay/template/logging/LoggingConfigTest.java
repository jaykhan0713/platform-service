package com.jay.template.logging;

import static org.junit.jupiter.api.Assertions.*;

import com.jay.template.logging.properties.MdcProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class LoggingConfigTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(LoggingConfig.class);

    @Test
    void mdcPropertiesIsRegisteredAsBean() {
        contextRunner.run(context -> {
            MdcProperties bean = context.getBean(MdcProperties.class);
            assertNotNull(bean);
        });
    }
}