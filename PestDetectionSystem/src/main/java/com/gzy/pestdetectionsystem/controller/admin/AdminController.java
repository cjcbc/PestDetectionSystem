package com.gzy.pestdetectionsystem.controller.admin;

import com.gzy.pestdetectionsystem.dto.user.ChangePasswordDTO;
import com.gzy.pestdetectionsystem.service.user.UserService;
import com.gzy.pestdetectionsystem.utils.Result;
import com.gzy.pestdetectionsystem.vo.user.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gzy.pestdetectionsystem.utils.Result.ok;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;


    @GetMapping("/info")
    public Result<UserVO> getUser(HttpServletRequest request) {
        return ok(userService.getProfile(request));
    }

    @GetMapping("/allusers")
    public Result<List<UserVO>> getAllUsers() {
        return ok(userService.getAllUsers());
    }

    @PatchMapping("/password")
    public Result<?> changePassword(@RequestBody ChangePasswordDTO dto, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        userService.changePassword(adminId, dto);
        return Result.ok("密码修改成功");
    }

    @PatchMapping("/users/{userId}/disable")
    public Result<?> disableUser(@PathVariable String userId, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        userService.disableUser(operatorId, userId);
        return Result.ok("用户已禁用");
    }

    @PatchMapping("/users/{userId}/enable")
    public Result<?> enableUser(@PathVariable String userId) {
        userService.enableUser(userId);
        return Result.ok("用户已启用");
    }

    @DeleteMapping("/users/{userId}")
    public Result<?> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ok("用户已删除");
    }

    @PatchMapping("/users/{userId}/role")
    public Result<?> setUserRole(@PathVariable String userId, @RequestBody Map<String, Integer> body) {
        Integer role = body.get("role");
        userService.setUserRole(userId, role);
        return ok("用户角色已修改");
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
        return ok(stats);
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        userService.logoutUser(request, token);
        return ok("登出成功");
    }
}
