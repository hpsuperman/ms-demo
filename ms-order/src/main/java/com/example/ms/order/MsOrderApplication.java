package com.example.ms.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.ms")
public class MsOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsOrderApplication.class, args);
    }
}
