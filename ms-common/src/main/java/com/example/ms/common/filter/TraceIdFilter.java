package com.example.ms.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * servlet 服务的 traceId：从网关的 X-Trace-Id 请求头取链路号，打进 MDC 供日志打印，
 * 没有则自己生成一个（服务被直连时）。响应头写回 X-Trace-Id，方便调用方排查。
 * 由 ms-user / ms-file 通过 scanBasePackages=com.example.ms 自动扫描生效。
 */
@Component
public class TraceIdFilter extends OncePerRequestFilter {

  public static final String TRACE_ID_HEADER = "X-Trace-Id";

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String traceId = request.getHeader(TRACE_ID_HEADER);
    if (traceId == null || traceId.isBlank()) {
      traceId = UUID.randomUUID().toString().replace("-", "");
    }
    MDC.put("traceId", traceId);
    response.setHeader(TRACE_ID_HEADER, traceId);
    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove("traceId");
    }
  }
}
