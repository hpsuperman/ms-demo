package com.example.ms.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// scanBasePackages=com.example.ms：让公共模块的 GlobalExceptionHandler 等组件能被扫描到
@SpringBootApplication(scanBasePackages = "com.example.ms")
public class MsUserApplication {

  public static void main(String[] args) {
    SpringApplication.run(MsUserApplication.class, args);
  }
}
