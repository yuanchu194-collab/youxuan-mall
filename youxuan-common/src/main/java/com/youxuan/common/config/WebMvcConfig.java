package com.youxuan.common.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;

/**
 * Web MVC 基础配置，统一处理响应编码等通用 Web 行为。
 */
@Configuration
public class WebMvcConfig {

    /**
     * 强制 Servlet 服务使用 UTF-8 编码，避免中文错误信息在客户端显示乱码。
     */
    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> youxuanCharacterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceRequestEncoding(true);
        filter.setForceResponseEncoding(true);

        FilterRegistrationBean<CharacterEncodingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Integer.MIN_VALUE);
        return registrationBean;
    }
}
