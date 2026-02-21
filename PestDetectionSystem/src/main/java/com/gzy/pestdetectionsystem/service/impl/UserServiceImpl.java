package com.gzy.pestdetectionsystem.service.impl;

import com.gzy.pestdetectionsystem.entity.User;
import com.gzy.pestdetectionsystem.exception.BusinessException;
import com.gzy.pestdetectionsystem.exception.CommonErrorCode;
import com.gzy.pestdetectionsystem.mapper.UserMapper;
import com.gzy.pestdetectionsystem.service.UserService;
import com.gzy.pestdetectionsystem.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<UserVo> getAllUsers() {
        List<User> users = userMapper.selectList(null);

        List<UserVo> userVoList = new ArrayList<>();
        for (User user : users) {
            UserVo userVo = new UserVo();
            userVo.setId(user.getId());
            userVo.setUsername(user.getUsername());
            userVo.setEmail(user.getEmail());
            userVo.setPhone(user.getPhone());
            userVo.setRole(user.getRole().getId());
            userVoList.add(userVo);
        }

        log.warn("获取所有用户信息");
        return userVoList;
    }

    @Override
    public UserVo getUserById(Long id) {
        User user = userMapper.selectById(id);
        UserVo userVo = new UserVo();

        userVo.setId(user.getId());
        userVo.setUsername(user.getUsername());
        userVo.setEmail(user.getEmail());
        userVo.setPhone(user.getPhone());

        return userVo;
    }

    @Override
    public void updateUsername(Long id, String username) {
        User user = userMapper.selectById(id);
        if (user == null)
            throw new BusinessException(CommonErrorCode.BIND_USER_NOT_EXISTS);
        log.info("id: {} 请求更新用户名：{}", id, username);

        user.setUsername(username);
        userMapper.updateById(user);
        log.info("id: {} 用户名更新为: {}", id, username);
    }

//    @Override
//    public UserVo getProfile(String token) {
//        if (token == null || !token.startsWith("Bearer ")) {
//            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
//        }
//
//        String jwt = token.substring(7);
//        Claims claims = jwtUtil.parseToken(jwt);
//        Long userId = Long.parseLong(claims.getSubject());
//
//        User user = userMapper.selectById(userId);
//        if (user == null) {
//            throw new BusinessException(40011, "用户不存在");
//        }
//
//        UserVo vo = new UserVo();
//        vo.setId(user.getId());
//        vo.setUsername(user.getUsername());
//        vo.setPhone(user.getPhone());
//        vo.setEmail(user.getEmail());
//        return vo;
//    }
//

    @Override
    public UserVo getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String token = (String) request.getAttribute("token");
        User user = userMapper.selectById(userId);
        UserVo userVo = new UserVo();
        userVo.setId(user.getId());
        userVo.setUsername(user.getUsername());
        userVo.setEmail(user.getEmail());
        userVo.setPhone(user.getPhone());
        userVo.setRole(user.getRole().getId());
        userVo.setToken(token);
        return userVo;
    }
}
