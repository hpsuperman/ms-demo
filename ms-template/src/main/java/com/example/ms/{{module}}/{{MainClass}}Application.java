package com.example.ms.{{module}};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.ms")
public class {{MainClass}}Application {
    public static void main(String[] args) {
        SpringApplication.run({{MainClass}}Application.class, args);
    }
}