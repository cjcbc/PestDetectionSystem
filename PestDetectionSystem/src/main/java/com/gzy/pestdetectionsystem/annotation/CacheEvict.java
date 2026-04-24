package com.gzy.pestdetectionsystem.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEvict {

    /**
     * 缓存 key 前缀，例如 "user:profile"
     */
    String prefix() default "";

    /**
     * 缓存 key 后缀，支持 SpEL 表达式，例如 "#userId"
     */
    String suffix() default "";

    /**
     * 是否在方法执行前删除缓存（默认 false，方法成功后删除）
     * true 适用于写操作前清缓存防脏读
     */
    boolean beforeInvocation() default false;
}