package com.gzy.pestdetectionsystem.service.impl;

import com.gzy.pestdetectionsystem.entity.User;
import com.gzy.pestdetectionsystem.exception.AuthException;
import com.gzy.pestdetectionsystem.mapper.UserMapper;
import com.gzy.pestdetectionsystem.service.UserService;
import com.gzy.pestdetectionsystem.utils.JwtUtil;
import com.gzy.pestdetectionsystem.vo.UserVo;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
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
            userVoList.add(userVo);
        }

        return userVoList;
    }

    @Override
    public UserVo getProfile(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new AuthException(401, "未登录");
        }

        String jwt = token.substring(7);
        Claims claims = jwtUtil.parseToken(jwt);
        Long userId = Long.parseLong(claims.getSubject());

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new AuthException(404, "用户不存在");
        }

        UserVo vo = new UserVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        return vo;
    }

}
