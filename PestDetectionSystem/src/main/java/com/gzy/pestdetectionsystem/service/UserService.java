package com.gzy.pestdetectionsystem.service;

import com.gzy.pestdetectionsystem.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService {

    /**
     * @return 所有用户信息
     */
    List<UserVO> getAllUsers();

    /**
     * @修改用户名
     */
    void updateUsername(Long id, String username);

    UserVO getUserById(Long id);
    UserVO getProfile(HttpServletRequest request);
}
