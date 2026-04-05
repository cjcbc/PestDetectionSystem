package com.gzy.pestdetectionsystem.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gzy.pestdetectionsystem.utils.Result;
import com.gzy.pestdetectionsystem.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class CommonInterceptor implements HandlerInterceptor {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
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
            writeResult(response, HttpServletResponse.SC_UNAUTHORIZED, "Bearer token is required");
            return false;
        }

        String token = authHeader.substring(7);

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

