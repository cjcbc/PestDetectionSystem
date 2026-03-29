package com.gzy.pestdetectionsystem.service.impl;

import com.gzy.pestdetectionsystem.dto.BindDTO;
import com.gzy.pestdetectionsystem.dto.LoginDTO;
import com.gzy.pestdetectionsystem.dto.RegisterDTO;
import com.gzy.pestdetectionsystem.entity.User;
import com.gzy.pestdetectionsystem.exception.BusinessException;
import com.gzy.pestdetectionsystem.exception.CommonErrorCode;
import com.gzy.pestdetectionsystem.mapper.UserMapper;
import com.gzy.pestdetectionsystem.service.AuthService;
import com.gzy.pestdetectionsystem.utils.JwtUtil;
import com.gzy.pestdetectionsystem.utils.PasswordUtil;
import com.gzy.pestdetectionsystem.utils.SnowflakeIdGenerator;
import com.gzy.pestdetectionsystem.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    public AuthServiceImpl(UserMapper userMapper, SnowflakeIdGenerator snowflakeIdGenerator) {
        this.userMapper = userMapper;
        this.snowflakeIdGenerator = snowflakeIdGenerator;
    }

    public void register(RegisterDTO dto) {
        if (Objects.isNull(dto)) {
            throw new BusinessException(CommonErrorCode.REGISTER_PARAM_INVALID);
        }
        log.info("用户注册请求，phone={}, email={}", dto.getPhone(), dto.getEmail());

        //手机号和邮箱至少有一个
        if ((dto.getPhone() == null || dto.getPhone().isBlank()) &&
                (dto.getEmail() == null || dto.getEmail().isBlank()))
            throw new BusinessException(CommonErrorCode.REGISTER_ACCOUNT_REQUIRED);

        //检查手机号或邮箱是否已存在
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            User existingPhone = userMapper.selectByPhone(dto.getPhone());
            if (existingPhone != null) throw new BusinessException(CommonErrorCode.REGISTER_PHONE_EXISTS);
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            User existingEmail = userMapper.selectByEmail(dto.getEmail());
            if (existingEmail != null) throw new BusinessException(CommonErrorCode.REGISTER_EMAIL_EXISTS);
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
        log.debug("注册用户准备入库，userId={}, username = {}", id, username);

        userMapper.insert(user);
        log.info("用户注册成功，userId={}, username = {}", id, username);
    }

    public UserVO login(LoginDTO dto) {
        if (Objects.isNull(dto)) {
            throw new BusinessException(CommonErrorCode.LOGIN_PARAM_INVALID);
        }
        log.info("用户登录请求，account={}", dto.getAccount());

        User user = userMapper.selectByAccount(dto.getAccount());

        if (user == null) {
            throw new BusinessException(CommonErrorCode.LOGIN_ACCOUNT_NOT_FOUND);
        }
        if (!PasswordUtil.verifyPassword(dto.getPassword(), user.getSalt(), user.getPassword())) {
            throw new BusinessException(CommonErrorCode.LOGIN_PASSWORD_ERROR);
        }
        log.debug("用户密码校验通过，userId={}", user.getId());

        String token = JwtUtil.createToken(user.getId(), user.getRole().getId());

        log.info("用户登录成功，userId={}", user.getId());
        return new UserVO(user.getId(), user.getRole().getId(), user.getUsername(), user.getEmail(), user.getPhone(), token);
    }


    //绑定手机号或邮箱
    public void bind(BindDTO dto, HttpServletRequest request) {
        if (Objects.isNull(dto)) {
            throw new BusinessException(CommonErrorCode.BIND_PARAM_INVALID);
        }
        Long userId = (Long) request.getAttribute("userId");
        dto.setId(userId);
        log.info("用户绑定请求，userId={}, bindType={}", dto.getId(), dto.getBindType());

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
                throw new BusinessException(CommonErrorCode.BIND_TYPE_NOT_SUPPORTED);
        }
    }

    private void updatePhone(BindDTO dto, User user) {
        if (dto.getPhone() == null || dto.getPhone().isBlank()) {
            throw new BusinessException(CommonErrorCode.BIND_PARAM_INVALID,"手机号不能为空");
        }
        if (user == null) {
            throw new BusinessException(CommonErrorCode.BIND_USER_NOT_EXISTS);
        }

        //检查手机号是否重复
        if (Objects.equals(user.getPhone(), dto.getPhone())){
            throw new BusinessException(CommonErrorCode.BIND_PHONE_SAME);
        }
        //检查手机号是否已被注册
        User check = userMapper.selectByPhone(dto.getPhone());
        if (check != null && !Objects.equals(check.getId(), user.getId())) {
            throw new BusinessException(CommonErrorCode.BIND_PHONE_EXISTS);
        }

        user.setPhone(dto.getPhone());
        userMapper.updateById(user);
        log.info("手机号绑定成功，userId={}, phone={}", user.getId(), dto.getPhone());
    }

    private void updateEmail(BindDTO dto, User user) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new BusinessException(CommonErrorCode.BIND_PARAM_INVALID,"邮箱不能为空");
        }
        if (user == null) {
            throw new BusinessException(CommonErrorCode.BIND_USER_NOT_EXISTS);
        }

        //检查邮箱是否重复
        if (Objects.equals(user.getEmail(), dto.getEmail())){
            throw new BusinessException(CommonErrorCode.BIND_EMAIL_SAME);
        }
        //检查邮箱是否重已被注册
        User check = userMapper.selectByEmail(dto.getEmail());
        if (check != null && !Objects.equals(check.getId(), user.getId())) {
            throw new BusinessException(CommonErrorCode.BIND_EMAIL_EXISTS);
        }

        user.setEmail(dto.getEmail());
        userMapper.updateById(user);
        log.info("邮箱绑定成功，userId={}, email={}", user.getId(), dto.getEmail());
    }
}
