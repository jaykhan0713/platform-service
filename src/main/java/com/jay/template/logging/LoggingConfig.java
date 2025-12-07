package com.jay.template.logging;

import com.jay.template.logging.mdc.MdcHeaderProperties;
import com.jay.template.logging.mdc.MdcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({MdcProperties.class, MdcHeaderProperties.class})
public class LoggingConfig {
}