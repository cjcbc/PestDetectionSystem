package com.gzy.pestdetectionsystem.controller;

import com.gzy.pestdetectionsystem.dto.BindDTO;
import com.gzy.pestdetectionsystem.dto.LoginDTO;
import com.gzy.pestdetectionsystem.dto.RegisterDTO;
import com.gzy.pestdetectionsystem.entity.User;
import com.gzy.pestdetectionsystem.service.AuthService;
import com.gzy.pestdetectionsystem.service.UserService;
import com.gzy.pestdetectionsystem.utils.Result;
import com.gzy.pestdetectionsystem.vo.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/all")
    public Result<List<UserVo>> getAllUsers() {
        return Result.ok(userService.getAllUsers());
    }

    @PostMapping("/login")
    public Result<UserVo> login(@RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO dto){
        authService.register(dto);
        return Result.ok("注册成功");
    }

    @PostMapping("/bind")
    public Result<?> bind(@RequestBody BindDTO dto){
        authService.bind(dto);
        return Result.ok("绑定成功");
    }

}
