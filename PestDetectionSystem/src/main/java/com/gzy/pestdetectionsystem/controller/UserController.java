package com.gzy.pestdetectionsystem.controller;

import com.gzy.pestdetectionsystem.dto.BindDTO;
import com.gzy.pestdetectionsystem.dto.LoginDTO;
import com.gzy.pestdetectionsystem.dto.RegisterDTO;
import com.gzy.pestdetectionsystem.service.AuthService;
import com.gzy.pestdetectionsystem.service.UserService;
import com.gzy.pestdetectionsystem.utils.Result;
import com.gzy.pestdetectionsystem.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO dto){
        authService.register(dto);
        return Result.ok("注册成功");
    }

    @PatchMapping("/bind")
    public Result<?> bind(@RequestBody BindDTO dto, HttpServletRequest request){
        authService.bind(dto,request);
        return Result.ok("绑定成功");
    }

    @GetMapping("/info")
    public Result<UserVO> info(HttpServletRequest request){
        return Result.ok(userService.getProfile(request));
    }

    @PatchMapping("/username")
    public Result<UserVO> updateUsername(@RequestParam String username, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        userService.updateUsername(userId, username);
        return Result.ok(userService.getProfile(userId));
    }

    @PatchMapping("sex")
    public Result<UserVO> updateSex(@RequestParam int sex, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        userService.updateSex(userId,sex);
        return Result.ok(userService.getProfile(userId));
    }

    @PostMapping("/avatar")
    public Result<?> updateAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        userService.updateImage(currentUserId, file);
        return Result.ok("头像更新成功");
    }

}
