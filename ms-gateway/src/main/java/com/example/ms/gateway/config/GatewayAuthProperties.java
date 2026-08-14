package com.example.ms.gateway.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 网关鉴权配置：免鉴权路径白名单（绑定 application.yml 的 gateway.auth.*） */
@Data
@Component
@ConfigurationProperties(prefix = "gateway.auth")
public class GatewayAuthProperties {

  /** 不需要登录即可访问的路径，支持精确路径或以 /** 结尾的前缀 */
  private List<String> whitelist = new ArrayList<>();
}
