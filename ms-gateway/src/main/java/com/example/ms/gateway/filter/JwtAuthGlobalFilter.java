package com.example.ms.gateway.filter;

import com.example.ms.gateway.config.GatewayAuthProperties;
import com.example.ms.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关统一 JWT 鉴权：白名单/OPTIONS 放行，其余请求校验 Authorization: Bearer <token>。
 * 校验通过后把用户身份（userId/phone/role）透传为 X-User-* 请求头给下游服务。
 * 失败返回 401，JSON 结构对齐 ms-common 的 ApiResponse。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

  private final JwtUtil jwtUtil;
  private final GatewayAuthProperties authProperties;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String path = request.getPath().value();

    // OPTIONS 预检请求直接放行（CORS 由 globalcors 处理）
    if (request.getMethod().name().equalsIgnoreCase("OPTIONS")) {
      return chain.filter(exchange);
    }
    // 白名单路径免鉴权（登录/注册/验证码/健康检查）
    if (isWhitelisted(path)) {
      return chain.filter(exchange);
    }

    String token = extractToken(request);
    if (token == null) {
      return unauthorized(exchange, "未登录：请携带 Authorization: Bearer <token>");
    }

    final Claims claims;
    try {
      claims = jwtUtil.parseToken(token);
    } catch (ExpiredJwtException e) {
      return unauthorized(exchange, "登录已过期，请重新登录");
    } catch (JwtException | IllegalArgumentException e) {
      return unauthorized(exchange, "无效的登录凭证");
    }

    // 鉴权通过：把用户身份转成请求头，转发给下游服务
    ServerHttpRequest mutated = request.mutate()
        .header("X-User-Id", String.valueOf(claims.get("userId")))
        .header("X-User-Phone", String.valueOf(claims.get("phone")))
        .header("X-User-Role", String.valueOf(claims.get("role")))
        .build();
    return chain.filter(exchange.mutate().request(mutated).build());
  }

  private boolean isWhitelisted(String path) {
    for (String pattern : authProperties.getWhitelist()) {
      if (pattern.endsWith("/**")) {
        if (path.startsWith(pattern.substring(0, pattern.length() - 3))) {
          return true;
        }
      } else if (path.equals(pattern) || path.startsWith(pattern + "/")) {
        return true;
      }
    }
    return false;
  }

  private String extractToken(ServerHttpRequest request) {
    String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith("Bearer ")) {
      return null;
    }
    return header.substring(7).trim();
  }

  private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    String body = "{\"code\":401,\"message\":\"" + message
        + "\",\"data\":null,\"timestamp\":\"" + LocalDateTime.now() + "\"}";
    DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
    return response.writeWith(Mono.just(buffer));
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 1; // 在 TraceIdWebFilter 之后执行
  }
}
