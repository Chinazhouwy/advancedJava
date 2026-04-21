package com.advancedjava.agentscope;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.advancedjava")
public class AgentScopeApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentScopeApplication.class, args);
    }
}

