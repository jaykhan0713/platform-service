package com.jay.template.infra.logging;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({MdcProperties.class, MdcPropertiesV1.class})
class LogConfig {}