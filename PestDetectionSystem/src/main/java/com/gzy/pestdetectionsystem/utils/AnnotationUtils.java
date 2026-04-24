package com.gzy.pestdetectionsystem.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

public class AnnotationUtils {

    private static final Logger log = LoggerFactory.getLogger(AnnotationUtils.class);
    private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    /**
     * 解析 SpEL 表达式，将方法参数名与实际参数值绑定后求值
     */
    public static String parseSpel(String key, Method method, Object[] args) {
        if (!StringUtils.hasLength(key)) {
            return "";
        }
        String[] paramNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
        StandardEvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        try {
            return SPEL_PARSER.parseExpression(key).getValue(context, String.class);
        } catch (EvaluationException e) {
            log.warn("SpEL parse failed for expression '{}': {}", key, e.getMessage());
            return "";
        }
    }

    /**
     * 执行目标方法并打印耗时日志
     */
    public static Object invokeTargetMethod(ProceedingJoinPoint joinPoint, Logger logger,
                                            String beforeMsg, String afterMsg) throws Throwable {
        long startTime = System.currentTimeMillis();
        logger.info(beforeMsg);
        Object result = joinPoint.proceed(joinPoint.getArgs());
        long duration = System.currentTimeMillis() - startTime;
        logger.info("{}, 耗时: {}ms", afterMsg, duration);
        return result;
    }
}