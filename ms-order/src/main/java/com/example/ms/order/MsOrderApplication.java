package com.example.ms.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.example.ms")
// 开启 Feign：扫描所有 @FeignClient 接口并生成代理实现
@EnableFeignClients
public class MsOrderApplication {

  public static void main(String[] args) {
    SpringApplication.run(MsOrderApplication.class, args);
  }
}
