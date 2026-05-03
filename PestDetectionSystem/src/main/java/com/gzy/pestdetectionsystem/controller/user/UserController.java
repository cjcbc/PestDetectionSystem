package com.gzy.pestdetectionsystem.controller.user;

import com.gzy.pestdetectionsystem.annotation.RateLimit;
import com.gzy.pestdetectionsystem.dto.user.BindDTO;
import com.gzy.pestdetectionsystem.dto.user.ChangePasswordDTO;
import com.gzy.pestdetectionsystem.dto.user.LoginDTO;
import com.gzy.pestdetectionsystem.dto.user.RegisterDTO;
import com.gzy.pestdetectionsystem.service.user.AuthService;
import com.gzy.pestdetectionsystem.service.user.UserService;
import com.gzy.pestdetectionsystem.service.user.VerificationCodeService;
import com.gzy.pestdetectionsystem.utils.Result;
import com.gzy.pestdetectionsystem.vo.user.UserVO;
import com.gzy.pestdetectionsystem.vo.user.VerificationCodeVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthService authService;
    private final VerificationCodeService verificationCodeService;

    @RateLimit
    @GetMapping("/verification-code")
    public Result<VerificationCodeVO> verificationCode() {
        return Result.ok(verificationCodeService.create());
    }

    @RateLimit
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @RateLimit
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO dto){
        authService.register(dto);
        return Result.ok("注册成功");
    }

    @PatchMapping("/bind")
    public Result<?> bind(@Valid @RequestBody BindDTO dto, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        authService.bind(userId, dto);
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

    @PatchMapping("/password")
    public Result<?> changePassword(@RequestBody ChangePasswordDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.changePassword(userId, dto);
        return Result.ok("密码修改成功");
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        userService.logoutUser(request, token);
        return Result.ok("登出成功");
    }

}
