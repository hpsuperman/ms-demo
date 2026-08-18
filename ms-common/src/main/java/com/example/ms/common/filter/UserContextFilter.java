package com.example.ms.common.filter;

import com.example.ms.common.context.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 把网关透传的 X-User-Id / X-User-Phone / X-User-Role 请求头收进 UserContext（ThreadLocal），
 * 供请求线程内任意层级取当前用户，与 TraceIdFilter 处理 X-Trace-Id 是同一套思路。
 * finally 必须 clear，否则线程池复用会串用户（经典生产事故）。
 * 认证（谁）由网关做，本过滤器只负责把身份放到上下文，不校验、不拦截。
 */
@Component
public class UserContextFilter extends OncePerRequestFilter {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_PHONE_HEADER = "X-User-Phone";
    public static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        UserContext context = new UserContext();
        context.setUserId(parseLong(request.getHeader(USER_ID_HEADER)));
        context.setPhone(request.getHeader(USER_PHONE_HEADER));
        context.setRole(request.getHeader(USER_ROLE_HEADER));
        UserContext.setContext(context);
        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
