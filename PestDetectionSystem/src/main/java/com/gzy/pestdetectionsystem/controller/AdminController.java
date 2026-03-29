package com.gzy.pestdetectionsystem.controller;

import com.gzy.pestdetectionsystem.service.UserService;
import com.gzy.pestdetectionsystem.utils.Result;
import com.gzy.pestdetectionsystem.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;

    @GetMapping("/allusers")
    public Result<List<UserVO>> getAllUsers() {
        return Result.ok(userService.getAllUsers());
    }

}
