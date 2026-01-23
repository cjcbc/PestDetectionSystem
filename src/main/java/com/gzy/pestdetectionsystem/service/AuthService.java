package com.gzy.pestdetectionsystem.service;

import com.gzy.pestdetectionsystem.dto.BindDTO;
import com.gzy.pestdetectionsystem.dto.LoginDTO;
import com.gzy.pestdetectionsystem.dto.RegisterDTO;
import com.gzy.pestdetectionsystem.vo.UserVo;

public interface AuthService {
    /**
     * 用户注册
     *
     * @param dto 前端传来的注册信息
     */
    public void register(RegisterDTO dto);

    /**
     * 用户登录
     *
     * @param dto 前端传来的登录信息（手机号/邮箱 + 密码）
     * @return 登录成功后返回用户信息和 token
     */
    public UserVo login(LoginDTO dto);

    /**
     * 用户绑定手机号或邮箱
     *
     * @param dto 前端传来的绑定信息（手机号/邮箱）
     */
    public void bind(BindDTO dto);
}
