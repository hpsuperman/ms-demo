package com.example.ms.user.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明式角色校验：标注在需要特定角色才能访问的接口方法上。
 * 校验逻辑由 RoleAspect 统一执行，读网关透传的 X-User-Role 请求头。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    String[] value() default {"ADMIN"};
}
