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
        registry.addInterceptor(new CommonInterceptor(Set.of(0)))
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login").excludePathPatterns("/health/**");

        registry.addInterceptor(new CommonInterceptor(Set.of(1)))
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/login")
                .excludePathPatterns("/user/register");
    }
}
