package com.gzy.pestdetectionsystem.config;

import com.gzy.pestdetectionsystem.interceptor.CommonInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Set;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 管理员拦截器（角色0）
        registry.addInterceptor(new CommonInterceptor(Set.of(0)))
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/api/admin/login");

        // 用户拦截器（角色1）
        registry.addInterceptor(new CommonInterceptor(Set.of(1)))
                .addPathPatterns("/user/**", "/detect/**", "/chat/**")
                .excludePathPatterns("/api/user/login", "/api/user/register");
    }
}
