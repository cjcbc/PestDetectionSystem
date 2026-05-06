package com.gzy.pestdetectionsystem.config.common;

import com.gzy.pestdetectionsystem.interceptor.CommonInterceptor;
import com.gzy.pestdetectionsystem.interceptor.RateLimitInterceptor;
import com.gzy.pestdetectionsystem.mapper.user.UserMapper;
import com.gzy.pestdetectionsystem.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RedisUtil redisUtil;
    private final UserMapper userMapper;
    private final RateLimitInterceptor rateLimitInterceptor;

    @Value("${user.base-path:./images}")
    private String pestImagesPath;

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
                        "/user/captcha",
                        "/user/login",
                        "/user/register",
                        "/user/sm2-public-key"
                );

        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置 pest-images 目录的静态资源映射
        // 访问路径：/images/filename.jpg
        // 实际路径：${PEST_IMAGES_PATH} 下的 pest-images 子目录
        String resourceLocation = "file:" + pestImagesPath + "/pest-images/";
        registry.addResourceHandler("/images/**")
                .addResourceLocations(resourceLocation);
    }
}
