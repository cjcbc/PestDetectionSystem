package com.gzy.pestdetectionsystem.controller;

import com.gzy.pestdetectionsystem.dto.ChangePasswordDTO;
import com.gzy.pestdetectionsystem.service.UserService;
import com.gzy.pestdetectionsystem.utils.JwtUtil;
import com.gzy.pestdetectionsystem.utils.Result;
import com.gzy.pestdetectionsystem.utils.RedisUtil;
import com.gzy.pestdetectionsystem.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final RedisUtil redisUtil;
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_PROFILE_CACHE_KEY_PREFIX = "user:profile:";


    @GetMapping("/info")
    public Result<UserVO> getUser(HttpServletRequest request) {
        return Result.ok(userService.getProfile(request));
    }

    @GetMapping("/allusers")
    public Result<List<UserVO>> getAllUsers() {
        return Result.ok(userService.getAllUsers());
    }

    @PatchMapping("/password")
    public Result<?> changePassword(@RequestBody ChangePasswordDTO dto, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        userService.changePassword(adminId, dto);
        return Result.ok("密码修改成功");
    }

    @PatchMapping("/users/{userId}/disable")
    public Result<Map<String, Object>> disableUser(@PathVariable String userId) {
        // TODO: 实现禁用用户逻辑
        userService.disableUser(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "用户已禁用");
        return Result.ok(result);
    }

    @PatchMapping("/users/{userId}/enable")
    public Result<Map<String, Object>> enableUser(@PathVariable String userId) {
        // TODO: 实现启用用户逻辑
        userService.enableUser(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "用户已启用");
        return Result.ok(result);
    }

    @DeleteMapping("/users/{userId}")
    public Result<Map<String, Object>> deleteUser(@PathVariable String userId) {
        // TODO: 实现删除用户逻辑
        userService.deleteUser(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "用户已删除");
        return Result.ok(result);
    }

    @PatchMapping("/users/{userId}/role")
    public Result<Map<String, Object>> setUserRole(@PathVariable String userId, @RequestBody Map<String, Integer> body) {
        // TODO: 实现修改用户角色逻辑
        Integer role = body.get("role");
        userService.setUserRole(userId, role);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "用户角色已修改");
        return Result.ok(result);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getSystemStats() {
        // TODO: 实现获取系统统计数据逻辑
        Map<String, Object> stats = new HashMap<>();
        List<UserVO> allUsers = userService.getAllUsers();
        stats.put("totalUsers", allUsers.size());
        stats.put("activeUsers", allUsers.size()); // 简化处理，实际应该统计活跃用户
        stats.put("totalPosts", 0); // 需要从PostService获取
        stats.put("pendingReview", 0); // 需要从审核服务获取
        stats.put("activeAlerts", 0); // 需要从预警服务获取
        return Result.ok(stats);
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        if (token != null) {
            long ttl = JwtUtil.getUserAuthTTLFromToken(token);// 毫秒
            // 添加 token 到黑名单
            redisUtil.set(TOKEN_BLACKLIST_PREFIX + token, "revoked", ttl / 1000);
        }
        return Result.ok("登出成功");
    }
}
