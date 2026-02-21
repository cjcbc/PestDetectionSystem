package com.gzy.pestdetectionsystem.interceptor;

import com.gzy.pestdetectionsystem.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class CommonInterceptor implements HandlerInterceptor {
    private final Set<Integer> allowedRoles;

    public CommonInterceptor(Set<Integer> allowedRoles) {
        this.allowedRoles = allowedRoles;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Bearer token is required");
            return false;
        }

        String token = authHeader.substring(7);

        Long userId;
        int roleId;

        try {
            userId = JwtUtil.getUserIdFromToken(token);
            roleId = JwtUtil.getRoleIdFromToken(token);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token out of date");
            return false;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid Token");
            return false;
        }

        if (allowedRoles.contains(roleId)) {
            request.setAttribute("roleId", roleId);
            request.setAttribute("userId", userId);
            request.setAttribute("token", token);
            return true;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("Access denied");
        return false;
    }
}

