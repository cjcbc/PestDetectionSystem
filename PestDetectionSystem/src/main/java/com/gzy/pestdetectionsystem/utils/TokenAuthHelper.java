package com.gzy.pestdetectionsystem.utils;

import com.gzy.pestdetectionsystem.exception.BusinessException;
import com.gzy.pestdetectionsystem.exception.CommonErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenAuthHelper {

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    private final RedisUtil redisUtil;

    public AuthUser requireUser(HttpServletRequest request) {
        AuthUser user = optionalUser(request);
        if (user == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        return user;
    }

    public AuthUser optionalUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        if (redisUtil.hasKey(TOKEN_BLACKLIST_PREFIX + token)) {
            throw new BusinessException(CommonErrorCode.TOKEN_INVALID, "Token has been revoked");
        }

        try {
            Long userId = JwtUtil.getUserIdFromToken(token);
            Integer roleId = JwtUtil.getRoleIdFromToken(token);
            return new AuthUser(userId, roleId);
        } catch (Exception ex) {
            throw new BusinessException(CommonErrorCode.TOKEN_INVALID);
        }
    }

    @Data
    @AllArgsConstructor
    public static class AuthUser {
        private Long userId;
        private Integer roleId;
    }
}
