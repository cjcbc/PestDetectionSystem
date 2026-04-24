package com.gzy.pestdetectionsystem.aspect;

import com.gzy.pestdetectionsystem.annotation.CacheEvict;
import com.gzy.pestdetectionsystem.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

import static com.gzy.pestdetectionsystem.utils.AnnotationUtils.parseSpel;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 1) //在事务结束后执行，避免脏读
public class CacheEvictAspect {

    private final RedisUtil redisUtil;

    @Around("@annotation(com.gzy.pestdetectionsystem.annotation.CacheEvict)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);

        String cacheKey = cacheEvict.prefix();
        if (StringUtils.hasLength(cacheEvict.suffix())) {
            String suffix = parseSpel(cacheEvict.suffix(), method, joinPoint.getArgs());
            cacheKey += ":" + suffix;
        }

        if (cacheEvict.beforeInvocation()) {
            redisUtil.del(cacheKey);
            log.info("{} - cache evicted (before): {}", method.getName(), cacheKey);
        }

        Object result = joinPoint.proceed();

        if (!cacheEvict.beforeInvocation()) {
            redisUtil.del(cacheKey);
            log.info("{} - cache evicted (after): {}", method.getName(), cacheKey);
        }

        return result;
    }
}