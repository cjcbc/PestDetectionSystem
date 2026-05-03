package com.gzy.pestdetectionsystem.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int limit() default 10;

    long window() default 60;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    String message() default "请求过于频繁，请稍后再试";
}
