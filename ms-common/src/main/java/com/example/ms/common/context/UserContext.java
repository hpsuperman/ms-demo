package com.example.ms.common.context;

/**
 * 请求级用户上下文：由 UserContextFilter 从网关透传的 X-User-* 请求头写入 ThreadLocal，
 * 请求线程内任何层级（Controller/Service/Mapper）都能用静态方法取当前用户。
 * 请求结束由过滤器 finally 清除，防止线程池复用线程时把上一个请求的用户泄露给下一个请求。
 * 服务被直连（没有网关）时无任何用户头，getContext() 返回 null，取到的字段都是 null，业务按需判空。
 */
public class UserContext {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    private Long userId;
    private String phone;
    private String role;

    public static void setContext(UserContext context) {
        CONTEXT.set(context);
    }

    public static UserContext getContext() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static Long getUserId() {
        return getContext() == null ? null : getContext().userId;
    }

    public static String getPhone() {
        return getContext() == null ? null : getContext().phone;
    }

    public static String getRole() {
        return getContext() == null ? null : getContext().role;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
