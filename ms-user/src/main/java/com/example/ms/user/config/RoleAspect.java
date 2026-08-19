package com.example.ms.user.config;

import com.example.ms.common.context.UserContext;
import com.example.ms.exception.BusinessException;
import com.example.ms.exception.ErrorCode;
import java.util.Arrays;
import java.util.List;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * @RequireRole 注解的权限校验切面：从 UserContext 取网关透传的当前用户角色。
 * 网关透传的 X-User-Role 可能是逗号分隔的多角色（如 "ADMIN,USER"），
 * 角色与注解声明的任一角色匹配即通过；一个都不匹配则抛 BusinessException，
 * 由 GlobalExceptionHandler 统一转成 403 响应。
 */
@Aspect
@Component
public class RoleAspect {

  @Before("@annotation(requireRole)")
  public void checkRole(RequireRole requireRole) {
    String roleHeader = UserContext.getRole();
    if (roleHeader == null || roleHeader.isBlank()) {
      throw new BusinessException(ErrorCode.FORBIDDEN, "需要" + Arrays.toString(requireRole.value()) + "角色权限");
    }
    List<String> userRoles = Arrays.asList(roleHeader.split(","));
    boolean allowed = Arrays.stream(requireRole.value()).anyMatch(userRoles::contains);
    if (!allowed) {
      throw new BusinessException(ErrorCode.FORBIDDEN, "需要" + Arrays.toString(requireRole.value()) + "角色权限");
    }
  }
}
