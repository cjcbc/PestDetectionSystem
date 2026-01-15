package com.gzy.pestdetectionsystem.service;

import com.gzy.pestdetectionsystem.vo.UserVo;

import java.util.List;

public interface UserService {

    /**
     * @return 所有用户信息
     */
    List<UserVo> getAllUsers();

    UserVo getProfile(String token);
}
git@github.com:cjcbc/PestDetectionSystem.git
