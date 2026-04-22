package com.advancedjava.agentscope;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目统一 Spring Boot 启动入口。
 *
 * <p>该类负责启动整个 {@code com.advancedjava} 包下的 Spring 组件扫描，
 * 便于把仓库中的控制器、配置类和演示 Bean 放入同一个应用上下文中运行。
 */
@SpringBootApplication(scanBasePackages = "com.advancedjava")
public class AgentScopeApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentScopeApplication.class, args);
    }
}
