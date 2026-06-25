package com.youxuan.common.config;

import com.youxuan.common.web.UserContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 用户上下文 MVC 配置，为下游 Servlet 服务统一注册请求拦截器。
 */
@Configuration
public class UserContextWebMvcConfig implements WebMvcConfigurer {

    /**
     * 注册用户上下文拦截器，所有下游接口都可从 UserContext 读取网关透传身份。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserContextInterceptor()).addPathPatterns("/**");
    }
}
