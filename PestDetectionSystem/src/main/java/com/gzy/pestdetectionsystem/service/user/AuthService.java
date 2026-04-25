package com.gzy.pestdetectionsystem.service.user;

import com.gzy.pestdetectionsystem.dto.user.BindDTO;
import com.gzy.pestdetectionsystem.dto.user.LoginDTO;
import com.gzy.pestdetectionsystem.dto.user.RegisterDTO;
import com.gzy.pestdetectionsystem.vo.user.UserVO;

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
    public UserVO login(LoginDTO dto);

    /**
     * 用户绑定手机号或邮箱
     *
     * @param userId 当前登录用户 ID
     * @param dto    前端传来的绑定信息（手机号/邮箱）
     */
    public void bind(Long userId, BindDTO dto);
}
