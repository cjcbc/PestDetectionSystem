package com.gzy.pestdetectionsystem.aspect;

import com.gzy.pestdetectionsystem.annotation.Cache;
import com.gzy.pestdetectionsystem.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.gzy.pestdetectionsystem.utils.AnnotationUtils.invokeTargetMethod;
import static com.gzy.pestdetectionsystem.utils.AnnotationUtils.parseSpel;

@Aspect
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class CacheAspect {

    private final RedisUtil redisUtil;

    @Around("@annotation(com.gzy.pestdetectionsystem.annotation.Cache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Cache cache = method.getAnnotation(Cache.class);

        // Build cache key
        String cacheKey = cache.prefix();
        if (StringUtils.hasLength(cache.suffix())) {
            String suffix = parseSpel(cache.suffix(), method, joinPoint.getArgs());
            cacheKey += ":" + suffix;
        }

        TimeUnit timeUnit = cache.timeUnit();
        long ttl = cache.ttl();
        int randomTime = cache.randomTime();

        // Safe random extra: skip when randomTime <= 0
        long randomExtra = randomTime > 0
                ? (long) ThreadLocalRandom.current().nextInt(randomTime)
                : 0;

        long totalTtlSeconds = timeUnit.toSeconds(ttl) + timeUnit.toSeconds(randomExtra);

        Class<?> clazz = joinPoint.getTarget().getClass();
        Logger logger = LoggerFactory.getLogger(clazz);

        if (cache.resetCache()) {
            return resetCache(joinPoint, method, cacheKey, totalTtlSeconds, logger);
        }

        return readFromCache(joinPoint, method, cacheKey, totalTtlSeconds, logger);
    }

    private Object resetCache(ProceedingJoinPoint joinPoint, Method method,
                              String cacheKey, long totalTtlSeconds,
                              Logger logger) throws Throwable {
        Object result = invokeTargetMethod(joinPoint, logger,
                String.format("%s - reset cache: %s", method.getName(), cacheKey),
                String.format("%s - reset cache done", method.getName()));

        if (result == null) {
            redisUtil.del(cacheKey);
            return null;
        }

        redisUtil.set(cacheKey, result, totalTtlSeconds);
        return result;
    }

    private Object readFromCache(ProceedingJoinPoint joinPoint, Method method,
                                 String cacheKey, long totalTtlSeconds,
                                 Logger logger) throws Throwable {
        Object cacheValue = redisUtil.get(cacheKey);

        if (cacheValue != null) {
            logger.info("{} - cache hit: {}", method.getName(), cacheKey);
            return cacheValue;
        }

        // Cache miss: execute target method
        Object result = invokeTargetMethod(joinPoint, logger,
                String.format("%s - cache miss: %s", method.getName(), cacheKey),
                String.format("%s - executed", method.getName()));

        if (result == null) {
            // Cache null value to prevent cache penetration
            redisUtil.setNullValue(cacheKey, totalTtlSeconds);
            return null;
        }

        redisUtil.set(cacheKey, result, totalTtlSeconds);
        return result;
    }
}