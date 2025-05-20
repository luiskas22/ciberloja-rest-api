package com.luis.ciberloja;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.luis.ciberloja", "com.luis.ciberloja.service", "com.luis.ciberloja.service.impl"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}