package com.advancedjava.interview.frameworks.spring;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 显式注册配置属性类。
 * 也可以使用 @ConfigurationPropertiesScan 自动扫描。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(InterviewAiProperties.class)
public class InterviewAiConfig {
}
