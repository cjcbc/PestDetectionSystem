package com.gzy.pestdetectionsystem.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gzy.pestdetectionsystem.annotation.RateLimit;
import com.gzy.pestdetectionsystem.utils.RedisUtil;
import com.gzy.pestdetectionsystem.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int TOO_MANY_REQUESTS = 429;

    private final RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true;
        }

        String key = buildKey(request, handlerMethod);
        long count = redisUtil.incr(key, 1);
        if (count == 1) {
            redisUtil.expire(key, rateLimit.timeUnit().toSeconds(rateLimit.window()));
        }

        if (count > rateLimit.limit()) {
            writeLimitResponse(response, rateLimit.message());
            return false;
        }

        return true;
    }

    private String buildKey(HttpServletRequest request, HandlerMethod handlerMethod) {
        String identity = resolveIdentity(request);
        return "rate_limit:" + handlerMethod.getBeanType().getName() + "." + handlerMethod.getMethod().getName() + ":" + identity;
    }

    private String resolveIdentity(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            return "user:" + userId;
        }
        return "ip:" + resolveClientIp(request);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    private void writeLimitResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(TOO_MANY_REQUESTS);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(Result.fail(TOO_MANY_REQUESTS, message)));
    }
}
