package com.example.ms.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.ms")
public class MsFileApplication {

  public static void main(String[] args) {
    SpringApplication.run(MsFileApplication.class, args);
  }
}
