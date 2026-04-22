package com.advancedjava.springwebflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * WebFlux 学习模块启动类。
 *
 * <p>你可以把它理解为“应用入口 main 方法 + Spring 容器启动器”。
 *
 * <p>@SpringBootApplication 是一个组合注解，等价于：
 * 1. @Configuration: 声明这是一个配置类。
 * 2. @EnableAutoConfiguration: 根据依赖自动装配（例如 WebFlux 相关 Bean）。
 * 3. @ComponentScan: 扫描并注册组件（Controller/Service 等）。
 *
 * <p>scanBasePackages 指定只扫描 com.advancedjava.springwebflux 包，
 * 这样你当前学习目录下的 Controller 都会被自动发现。
 */
@SpringBootApplication(scanBasePackages = "com.advancedjava.springwebflux")
public class SpringWebFluxDemoApplication {

    /**
     * Java 程序主入口。
     *
     * @param args 启动参数（可以通过命令行传递，例如 --server.port=8081）
     */
    public static void main(String[] args) {
        // 启动 Spring Boot 应用，并初始化 Web 服务器（默认 Netty 或容器配置）。
        SpringApplication.run(SpringWebFluxDemoApplication.class, args);
    }
}
