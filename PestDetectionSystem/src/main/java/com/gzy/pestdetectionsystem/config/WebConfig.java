package com.gzy.pestdetectionsystem.config;

import com.gzy.pestdetectionsystem.interceptor.CommonInterceptor;
import com.gzy.pestdetectionsystem.mapper.UserMapper;
import com.gzy.pestdetectionsystem.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RedisUtil redisUtil;
    private final UserMapper userMapper;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注意：拦截器在 context-path (/api) 处理后，所以这里的路径不包含 /api 前缀
        
        // 管理员拦截器（角色0）- 拦截 /admin 路径
        registry.addInterceptor(new CommonInterceptor(Set.of(0), redisUtil, userMapper))
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login");

        // 用户拦截器（角色0和1）- 拦截 /user /detect /chat 路径
        registry.addInterceptor(new CommonInterceptor(Set.of(0, 1), redisUtil, userMapper))
                .addPathPatterns("/user/**", "/detect/**", "/chat/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置 pest-images 目录的静态资源映射
        // 访问路径：/images/filename.jpg
        // 实际路径：D:\SHU files\Graduation project\PestDetectionSystem\pest-images\filename.jpg
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:D:/SHU files/Graduation project/PestDetectionSystem/pest-images/");
    }
}
