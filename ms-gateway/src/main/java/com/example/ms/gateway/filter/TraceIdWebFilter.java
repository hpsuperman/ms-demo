package com.example.ms.gateway.filter;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 网关侧 traceId：请求进来生成/透传一个 traceId，
 * 打进 MDC（本网关日志），并经 X-Trace-Id 请求头转发给下游服务（整条链路共用）。
 * 注意：WebFlux 线程切换下网关自身部分日志可能不带 traceId，属已知限制。
 */
@Component
public class TraceIdWebFilter implements WebFilter, Ordered {

  public static final String TRACE_ID_HEADER = "X-Trace-Id";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);
    if (traceId == null || traceId.isBlank()) {
      traceId = UUID.randomUUID().toString().replace("-", "");
    }
    final String finalTraceId = traceId;
    MDC.put("traceId", finalTraceId);
    exchange.getResponse().getHeaders().set(TRACE_ID_HEADER, finalTraceId);
    ServerWebExchange mutated = exchange.mutate()
        .request(r -> r.headers(h -> h.set(TRACE_ID_HEADER, finalTraceId)))
        .build();
    return chain.filter(mutated).doFinally(signal -> MDC.remove("traceId"));
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE; // 最先执行，先于 JWT 鉴权过滤器
  }
}
