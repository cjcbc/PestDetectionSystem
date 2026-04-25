package com.gzy.pestdetectionsystem.service.impl.user;

import com.gzy.pestdetectionsystem.assembler.user.UserAssembler;
import com.gzy.pestdetectionsystem.annotation.Cache;
import com.gzy.pestdetectionsystem.annotation.CacheEvict;
import com.gzy.pestdetectionsystem.config.common.UserProperties;
import com.gzy.pestdetectionsystem.dto.user.ChangePasswordDTO;
import com.gzy.pestdetectionsystem.entity.user.User;
import com.gzy.pestdetectionsystem.exception.BusinessException;
import com.gzy.pestdetectionsystem.exception.CommonErrorCode;
import com.gzy.pestdetectionsystem.mapper.user.UserMapper;
import com.gzy.pestdetectionsystem.service.user.UserService;
import com.gzy.pestdetectionsystem.utils.JwtUtil;
import com.gzy.pestdetectionsystem.utils.PasswordUtil;
import com.gzy.pestdetectionsystem.utils.RedisUtil;
import com.gzy.pestdetectionsystem.vo.user.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserProperties userProperties;
    private final RedisUtil redisUtil;
    private final UserAssembler userAssembler;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_PROFILE_CACHE_KEY_PREFIX = "user:profile";

    private UserService self;

    @Autowired
    public void setSelf(@Lazy UserService self) {
        this.self = self;
    }

    public UserServiceImpl(UserMapper userMapper, UserProperties userProperties, RedisUtil redisUtil, UserAssembler userAssembler) {
        this.userMapper = userMapper;
        this.userProperties = userProperties;
        this.redisUtil = redisUtil;
        this.userAssembler = userAssembler;
    }

    public List<UserVO> getAllUsers() {
        List<User> users = userMapper.selectList(null);
        log.info("获取所有用户信息");
        return users.stream().map(userAssembler::toListVO).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(CommonErrorCode.BIND_USER_NOT_EXISTS);
        }
        return userAssembler.toBasicVO(user);
    }

    @Override
    @Transactional
    @CacheEvict(prefix = "user:profile", suffix = "#id")
    public void updateUsername(Long id, String username) {
        User user = userMapper.selectByIdForUpdate(id);
        if (user == null)
            throw new BusinessException(CommonErrorCode.BIND_USER_NOT_EXISTS);
        if (user.getUsername().equals(username))
            return;
        user.setUsername(username);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    @CacheEvict(prefix = "user:profile", suffix = "#id")
    public void updateSex(Long id, int sex){
        User user = userMapper.selectByIdForUpdate(id);
        if (user == null)
            throw new BusinessException(CommonErrorCode.BIND_USER_NOT_EXISTS);
        if (sex == user.getSex())
            return;
        user.setSex(sex);
        userMapper.updateById(user);
    }


    private UserVO getUserInfo(Long userId) {
        log.info("user profile querying database, userId={}", userId);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(CommonErrorCode.BIND_USER_NOT_EXISTS);
        }
        return userAssembler.toProfileVO(user);
    }

    @Override
    public void evictProfileCache(Long userId) {
        if (userId == null) {
            return;
        }
        redisUtil.del("user:profile:" + userId);
        log.info("user profile cache evicted, userId={}", userId);
    }

    // 登录用返回信息 + token
    @Override
    public UserVO getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String token = (String) request.getAttribute("token");
        UserVO userVo = self.getProfile(userId);
        userVo.setToken(token);
        return userVo;
    }

    // 查询信息用
    @Override
    @Cache(prefix = "user:profile", suffix = "#userId", ttl = 30, randomTime = 5, timeUnit = TimeUnit.MINUTES)
    public UserVO getProfile(Long userId) {
        return getUserInfo(userId);
    }

    @Override
    @Transactional
    @CacheEvict(prefix = "user:profile", suffix = "#userId")
    public void updateImage(Long userId, MultipartFile file) {
        User user = userMapper.selectByIdForUpdate(userId);
        if (user == null) {
            throw new BusinessException(CommonErrorCode.BIND_USER_NOT_EXISTS);
        }
        
        if (file == null || file.isEmpty()) {
            throw new BusinessException(CommonErrorCode.USER_AVATAR_EMPTY);
        }
        
        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(CommonErrorCode.USER_AVATAR_TYPE_INVALID);
        }
        
        // 校验文件大小 (限制5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(CommonErrorCode.USER_AVATAR_TOO_LARGE);
        }
        
        // 保存目录
        String saveDir = userProperties.getBasePath();
        try {
            Files.createDirectories(Paths.get(saveDir));
        } catch (IOException e) {
            throw new RuntimeException("创建头像目录失败", e);
        }
        
        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "png";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }
        
        // 生成文件名
        String fileName = userId + "_" + System.currentTimeMillis() + "." + extension;
        String filePath = saveDir + "/" + fileName;
        
        // 保存文件
        try {
            file.transferTo(Paths.get(filePath).toFile());
        } catch (IOException e) {
            throw new RuntimeException("保存头像失败", e);
        }
        
        // 更新数据库
        user.setImage(filePath);
        userMapper.updateById(user);
        log.info("用户 {} 头像更新为: {}", userId, filePath);
    }

    @Override
    @Transactional
    @CacheEvict(prefix = "user:profile", suffix = "#userId")
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        // 参数校验
        if (dto == null || dto.getOldPassword() == null || dto.getNewPassword() == null || dto.getConfirmPassword() == null) {
            throw new BusinessException(CommonErrorCode.CHANGE_PASSWORD_PARAM_INVALID);
        }

        // 获取用户信息
        User user = userMapper.selectByIdForUpdate(userId);
        if (user == null) {
            throw new BusinessException(CommonErrorCode.BIND_USER_NOT_EXISTS);
        }

        // 验证原密码
        if (!PasswordUtil.verifyPassword(dto.getOldPassword(), user.getSalt(), user.getPassword())) {
            throw new BusinessException(CommonErrorCode.CHANGE_PASSWORD_OLD_PASSWORD_WRONG);
        }

        // 检查新密码是否与原密码相同
        if (PasswordUtil.verifyPassword(dto.getNewPassword(), user.getSalt(), user.getPassword())) {
            throw new BusinessException(CommonErrorCode.CHANGE_PASSWORD_SAME_AS_OLD);
        }

        // 检查两次输入的新密码是否一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(CommonErrorCode.CHANGE_PASSWORD_NOT_MATCH);
        }

        // 生成新的盐值和加密密码
        String newSalt = PasswordUtil.generateSalt();
        String newEncryptedPassword = PasswordUtil.encryptPassword(dto.getNewPassword(), newSalt);

        // 更新数据库
        user.setSalt(newSalt);
        user.setPassword(newEncryptedPassword);
        userMapper.updateById(user);
        log.info("用户 {} 密码已修改", userId);
    }

    @Override
    @Transactional
    @CacheEvict(prefix = "user:profile", suffix = "#userId")
    public void disableUser(Long operatorId, String userId) {
        Long id = Long.parseLong(userId);
        if (Objects.equals(operatorId, id)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN, "不能禁用自己");
        }
        User user = userMapper.selectByIdForUpdate(id);
        if (user == null) {
            throw new BusinessException(CommonErrorCode.BIND_USER_NOT_EXISTS);
        }
        user.setStatus(0);
        userMapper.updateById(user);
        log.info("用户已禁用: {}", userId);
    }

    @Override
    @Transactional
    @CacheEvict(prefix = "user:profile", suffix = "#userId")
    public void enableUser(String userId) {
        Long id = Long.parseLong(userId);
        User user = userMapper.selectByIdForUpdate(id);
        if (user == null) {
            throw new BusinessException(CommonErrorCode.BIND_USER_NOT_EXISTS);
        }
        user.setStatus(1);
        userMapper.updateById(user);
        log.info("用户已启用: {}", userId);
    }

    @Override
    @Transactional
    @CacheEvict(prefix = "user:profile", suffix = "#userId")
    public void deleteUser(String userId) {
        Long id = Long.parseLong(userId);
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(CommonErrorCode.BIND_USER_NOT_EXISTS);
        }
        userMapper.deleteById(id);
        log.info("用户已删除: {}", userId);
    }

    @Override
    @Transactional
    @CacheEvict(prefix = "user:profile", suffix = "#userId")
    public void setUserRole(String userId, Integer role) {
        Long id = Long.parseLong(userId);
        User user = userMapper.selectByIdForUpdate(id);
        if (user == null) {
            throw new BusinessException(CommonErrorCode.BIND_USER_NOT_EXISTS);
        }
        // role对应的是enum，需要根据id找到对应的Role
        // 假设role 0为admin，1为user，需要根据实际的Role enum调整
        log.info("用户角色已修改: {} -> {}", userId, role);
    }

    @Override
    @CacheEvict(prefix = USER_PROFILE_CACHE_KEY_PREFIX, suffix = "#request.getAttribute('userId')")
    public void logoutUser(HttpServletRequest request, String token) {
        if (token != null) {
            long ttlMs = JwtUtil.getUserAuthTTLFromToken(token);
            redisUtil.set(TOKEN_BLACKLIST_PREFIX + token, "revoked", ttlMs / 1000);
        }
        log.info("用户: {} 退出登录", JwtUtil.getUserIdFromToken(token));
    }

}
