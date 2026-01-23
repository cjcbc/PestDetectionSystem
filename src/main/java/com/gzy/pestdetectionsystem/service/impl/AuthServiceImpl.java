package com.gzy.pestdetectionsystem.service.impl;

import com.gzy.pestdetectionsystem.dto.BindDTO;
import com.gzy.pestdetectionsystem.dto.LoginDTO;
import com.gzy.pestdetectionsystem.dto.RegisterDTO;
import com.gzy.pestdetectionsystem.entity.User;
import com.gzy.pestdetectionsystem.exception.AuthException;
import com.gzy.pestdetectionsystem.mapper.UserMapper;
import com.gzy.pestdetectionsystem.service.AuthService;
import com.gzy.pestdetectionsystem.utils.JwtUtil;
import com.gzy.pestdetectionsystem.utils.PasswordUtil;
import com.gzy.pestdetectionsystem.utils.SnowflakeIdGenerator;
import com.gzy.pestdetectionsystem.vo.UserVo;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    public AuthServiceImpl(UserMapper userMapper, SnowflakeIdGenerator snowflakeIdGenerator) {
        this.userMapper = userMapper;
        this.snowflakeIdGenerator = snowflakeIdGenerator;
    }

    public void register(RegisterDTO dto) {
        //手机号和邮箱至少有一个
        if ((dto.getPhone() == null || dto.getPhone().isBlank()) &&
                (dto.getEmail() == null || dto.getEmail().isBlank())) {
            throw new AuthException(400, "手机号或邮箱至少填写一个");
        }

        //检查手机号或邮箱是否已存在
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            User existingPhone = userMapper.selectByPhone(dto.getPhone());
            if (existingPhone != null) {
                throw new AuthException(400, "手机号已注册");
            }
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            User existingEmail = userMapper.selectByEmail(dto.getEmail());
            if (existingEmail != null) {
                throw new AuthException(400, "邮箱已注册");
            }
        }

        String salt = PasswordUtil.generateSalt();
        String encrypted = PasswordUtil.encryptPassword(dto.getPassword(), salt);

        Long id = snowflakeIdGenerator.nextId();
        String username = dto.getUsername();

        //用户名为空则设置为默认昵称
        if(username == null || username.isBlank()){
            username = "uid_" + id.toString();
        }

        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setPassword(encrypted);
        user.setSalt(salt);
        user.setCreatedTime(System.currentTimeMillis());

        userMapper.insert(user);
    }

    public UserVo login(LoginDTO dto) {
        User user = userMapper.selectByAccount(dto.getAccount());

        if (user == null) {
            throw new AuthException(400, "手机号或邮箱不存在");
        }
        if (!PasswordUtil.verifyPassword(dto.getPassword(), user.getSalt(), user.getPassword())) {
            throw new AuthException(401, "密码错误");
        }
        String token = JwtUtil.createToken(user.getId());
        return new UserVo(user.getId(), user.getUsername(), user.getEmail(), user.getPhone(), token);
    }


    //绑定手机号或邮箱
    public void bind(BindDTO dto) {
        if (dto.getId() == null){
            throw new AuthException(402,"用户信息不合法");
        }
        if (dto.getBindType() == null){
            throw new AuthException(403,"不支持的绑定类型");
        }

        User user = userMapper.selectById(dto.getId());

        //确定要绑定哪个
        switch (dto.getBindType()) {
            case PHONE:
                updatePhone(dto, user);
                break;
            case EMAIL:
                updateEmail(dto, user);
                break;
            case BOTH:
                updatePhone(dto, user);
                updateEmail(dto, user);
                break;
            default:
                throw new AuthException(406, "不支持的绑定类型");
        }
    }

    private void updatePhone(BindDTO dto, User user) {
        if (dto.getPhone() == null || dto.getPhone().isBlank()) {
            throw new AuthException(401,"手机号不能为空");
        }
        if (user == null) {
            throw new AuthException(400,"用户不存在");
        }

        //检查手机号是否重复
        if (Objects.equals(user.getPhone(), dto.getPhone())){
            throw new AuthException(409,"手机号重复");
        }
        //检查手机号是否已被注册
        User check = userMapper.selectByPhone(dto.getPhone());
        if (check != null && !Objects.equals(check.getId(), user.getId())) {
            throw new AuthException(403,"手机号已存在");
        }


        user.setPhone(dto.getPhone());
        userMapper.updateById(user);
    }

    private void updateEmail(BindDTO dto, User user) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new AuthException(402, "邮箱不能为空");
        }
        if (user == null) {
            throw new AuthException(400,"用户不存在");
        }

        //检查邮箱是否重复
        if (Objects.equals(user.getEmail(), dto.getEmail())){
            throw new AuthException(409,"邮箱重复");
        }
        //检查邮箱是否重已被注册
        User check = userMapper.selectByEmail(dto.getEmail());
        if (check != null && !Objects.equals(check.getId(), user.getId())) {
            throw new AuthException(403,"邮箱已存在");
        }


        user.setEmail(dto.getEmail());
        userMapper.updateById(user);
    }
}
