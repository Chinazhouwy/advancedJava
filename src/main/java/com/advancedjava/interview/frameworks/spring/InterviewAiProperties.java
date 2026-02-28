package com.advancedjava.interview.frameworks.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Spring Boot 中 record 的典型用法：
 * 把配置文件映射成不可变配置对象，避免运行时被意外修改。
 *
 * application.yml 示例：
 * interview:
 *   ai:
 *     model: gpt-4o-mini
 *     timeout: 30s
 *     retry:
 *       max-attempts: 3
 *       backoff: 2s
 */
@ConfigurationProperties(prefix = "interview.ai")
public record InterviewAiProperties(
        @DefaultValue("gpt-4o-mini") String model,
        @DefaultValue("30s") Duration timeout,
        Retry retry
) {
    public record Retry(
            @DefaultValue("3") int maxAttempts,
            @DefaultValue("2s") Duration backoff
    ) {
    }
}
