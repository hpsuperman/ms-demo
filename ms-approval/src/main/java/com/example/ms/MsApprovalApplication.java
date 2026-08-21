package com.example.ms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.example.ms")
@EnableFeignClients
public class MsApprovalApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsApprovalApplication.class, args);
    }
}