package com.example.ms.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication(scanBasePackages = "com.example.ms")
public class MsStockApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsStockApplication.class, args);
    }
}