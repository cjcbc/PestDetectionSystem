package com.gzy.pestdetectionsystem.service;

import com.gzy.pestdetectionsystem.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService {

    /**
     * @return 所有用户信息
     */
    List<UserVo> getAllUsers();

    /**
     * @修改用户名
     */
    void updateUsername(Long id, String username);

    UserVo getUserById(Long id);
    UserVo getProfile(HttpServletRequest request);
}
