package com.example.ms.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * 网关侧 JWT 工具：只负责校验 ms-user 签发的 token，不签发。
 * 与 ms-user 的 JwtUtil 共用同一把密钥（jwt.secret），保证能验签。
 */
@Component
public class JwtUtil {

  private final SecretKey key;

  public JwtUtil(@Value("${jwt.secret}") String secret) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
  }

  /** 验证并解析 token，无效/过期会抛 JwtException */
  public Claims parseToken(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
