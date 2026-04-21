package com.advancedjava.springwebflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.advancedjava.springwebflux")
public class SpringWebFluxDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringWebFluxDemoApplication.class, args);
    }
}
