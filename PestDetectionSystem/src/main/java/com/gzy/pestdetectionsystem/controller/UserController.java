package com.gzy.pestdetectionsystem.controller;

import com.gzy.pestdetectionsystem.dto.BindDTO;
import com.gzy.pestdetectionsystem.dto.LoginDTO;
import com.gzy.pestdetectionsystem.dto.RegisterDTO;
import com.gzy.pestdetectionsystem.entity.User;
import com.gzy.pestdetectionsystem.service.AuthService;
import com.gzy.pestdetectionsystem.service.UserService;
import com.gzy.pestdetectionsystem.utils.Result;
import com.gzy.pestdetectionsystem.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/login")
    public Result<UserVo> login(@RequestBody LoginDTO dto) {
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
    public Result<UserVo> info(HttpServletRequest request){
        return Result.ok(userService.getProfile(request));
    }

    @PatchMapping("/{id}/username")
    public Result<UserVo> updateUsername(@PathVariable Long id, @RequestParam String username){
        userService.updateUsername(id, username);
        return Result.ok(userService.getUserById(id));
    }

}
