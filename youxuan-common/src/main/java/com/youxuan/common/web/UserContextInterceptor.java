package com.youxuan.common.web;

import com.youxuan.common.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 下游服务用户上下文拦截器，从网关透传请求头中读取当前用户信息。
 */
public class UserContextInterceptor implements HandlerInterceptor {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    /**
     * 请求进入 Controller 前写入 UserContext，供业务代码读取当前用户。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(HEADER_USER_ID);
        String role = request.getHeader(HEADER_USER_ROLE);
        if (StringUtils.hasText(userId) && StringUtils.hasText(role)) {
            UserContext.setUser(Long.valueOf(userId), null, role);
        }
        return true;
    }

    /**
     * 请求结束后清理 ThreadLocal，避免 Tomcat 线程复用导致用户信息串号。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
