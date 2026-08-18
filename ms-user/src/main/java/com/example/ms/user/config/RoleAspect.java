package com.example.ms.user.config;

import com.example.ms.common.context.UserContext;
import com.example.ms.exception.BusinessException;
import com.example.ms.exception.ErrorCode;
import java.util.Arrays;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * @RequireRole 注解的权限校验切面：从 UserContext 取网关透传的当前用户角色，
 * 角色不在注解声明的数组里则抛 BusinessException，由 GlobalExceptionHandler 统一转成 403 响应。
 */
@Aspect
@Component
public class RoleAspect {

  @Before("@annotation(requireRole)")
  public void checkRole(RequireRole requireRole) {
    String role = UserContext.getRole();
    if (!Arrays.asList(requireRole.value()).contains(role)) {
      throw new BusinessException(ErrorCode.FORBIDDEN, "需要" + Arrays.toString(requireRole.value()) + "角色权限");
    }
  }
}
