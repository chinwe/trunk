package com.test.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author chinwe
 * 2024/10/5
 */
@SpringBootApplication(scanBasePackages = "com.test.demo")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
