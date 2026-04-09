package com.gzy.pestdetectionsystem.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gzy.pestdetectionsystem.utils.Result;
import com.gzy.pestdetectionsystem.utils.JwtUtil;
import com.gzy.pestdetectionsystem.utils.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class CommonInterceptor implements HandlerInterceptor {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private final Set<Integer> allowedRoles;
    private final RedisUtil redisUtil;

    public CommonInterceptor(Set<Integer> allowedRoles, RedisUtil redisUtil) {
        this.allowedRoles = allowedRoles;
        this.redisUtil = redisUtil;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeResult(response, HttpServletResponse.SC_UNAUTHORIZED, "Bearer token is required");
            return false;
        }

        String token = authHeader.substring(7);

        // 检查 Token 黑名单
        if (redisUtil.hasKey(TOKEN_BLACKLIST_PREFIX + token)) {
            writeResult(response, HttpServletResponse.SC_UNAUTHORIZED, "Token has been revoked");
            return false;
        }

        Long userId;
        int roleId;

        try {
            userId = JwtUtil.getUserIdFromToken(token);
            roleId = JwtUtil.getRoleIdFromToken(token);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            writeResult(response, HttpServletResponse.SC_UNAUTHORIZED, "Token out of date");
            return false;
        } catch (Exception e) {
            writeResult(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
            return false;
        }

        if (allowedRoles.contains(roleId)) {
            request.setAttribute("roleId", roleId);
            request.setAttribute("userId", userId);
            request.setAttribute("token", token);
            return true;
        }

        writeResult(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
        return false;
    }

    private void writeResult(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(Result.fail(status, message)));
    }
}

