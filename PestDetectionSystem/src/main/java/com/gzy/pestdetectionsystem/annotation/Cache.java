package com.gzy.pestdetectionsystem.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Inherited
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {

    /**
     * 缓存 key 前缀，例如 "user:profile"
     */
    String prefix() default "";

    /**
     * 缓存 key 后缀，支持 SpEL 表达式，例如 "#userId"
     */
    String suffix() default "";

    /**
     * 缓存基础过期时间，默认 300 秒
     */
    long ttl() default 300;

    /**
     * 随机额外过期时间，防止缓存雪崩，默认 5
     * 当 randomTime <= 0 时不添加随机时间
     */
    int randomTime() default 5;

    /**
     * 缓存时间单位，默认秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 如果为 true 则执行目标方法后重设缓存
     * 目标方法必须返回需要缓存的数据
     */
    boolean resetCache() default false;
}