package com.gzy.pestdetectionsystem.service.impl;

import com.gzy.pestdetectionsystem.config.UserProperties;
import com.gzy.pestdetectionsystem.entity.User;
import com.gzy.pestdetectionsystem.exception.BusinessException;
import com.gzy.pestdetectionsystem.exception.CommonErrorCode;
import com.gzy.pestdetectionsystem.mapper.UserMapper;
import com.gzy.pestdetectionsystem.service.UserService;
import com.gzy.pestdetectionsystem.utils.RedisUtil;
import com.gzy.pestdetectionsystem.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private static final long USER_PROFILE_CACHE_TTL_SECONDS = 30 * 60;
    private static final String USER_PROFILE_CACHE_KEY_PREFIX = "user:profile:";

    private final UserMapper userMapper;
    private final UserProperties userProperties;
    private final RedisUtil redisUtil;

    public UserServiceImpl(UserMapper userMapper, UserProperties userProperties, RedisUtil redisUtil) {
        this.userMapper = userMapper;
        this.userProperties = userProperties;
        this.redisUtil = redisUtil;
    }

    public List<UserVO> getAllUsers() {
        List<User> users = userMapper.selectList(null);

        List<UserVO> userVOList = new ArrayList<>();
        for (User user : users) {
            // 列表接口不返回头像base64，避免数据过大
            UserVO userVo = new UserVO();
            userVo.setId(user.getId());
            userVo.setUsername(user.getUsername());
            userVo.setEmail(user.getEmail());
            userVo.setPhone(user.getPhone());
            userVo.setRole(user.getRole().getId());
            userVo.setSex(user.getSex() == null ? 2 : user.getSex());
            userVo.setFlag(user.getFlag() == null ? 0 : user.getFlag());
            userVo.setStatus(user.getStatus() == null ? 1 : user.getStatus());
            // image字段不设置，列表不返回头像
            userVOList.add(userVo);
        }

        log.info("获取所有用户信息");
        return userVOList;
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(CommonErrorCode.BIND_USER_NOT_EXISTS);
        }
        
        UserVO userVo = new UserVO();
        userVo.setId(user.getId());
        userVo.setUsername(user.getUsername());
        userVo.setEmail(user.getEmail());
        userVo.setPhone(user.getPhone());

        return userVo;
    }

    @Override
    @Transactional
    public void updateUsername(Long id, String username) {
        User user = userMapper.selectByIdForUpdate(id);
        if (user == null)
            throw new BusinessException(CommonErrorCode.BIND_USER_NOT_EXISTS);
        if (user.getUsername().equals(username))
            return;
        user.setUsername(username);
        userMapper.updateById(user);
        evictUserProfileCache(id);
    }

    @Override
    @Transactional
    public void updateSex(Long id, int sex){
        User user = userMapper.selectByIdForUpdate(id);
        if (user == null)
            throw new BusinessException(CommonErrorCode.BIND_USER_NOT_EXISTS);
        if (sex == user.getSex())
            return;
        user.setSex(sex);
        userMapper.updateById(user);
        evictUserProfileCache(id);
    }


    private String convertImageToBase64(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        try {
            Path path = Paths.get(imagePath);
            log.info(String.valueOf(path));
            if (Files.exists(path)) {
                byte[] imageBytes = Files.readAllBytes(path);
                String extension = imagePath.substring(imagePath.lastIndexOf('.') + 1).toLowerCase();
                String mimeType = switch (extension) {
                    case "png" -> "image/png";
                    case "gif" -> "image/gif";
                    case "webp" -> "image/webp";
                    default -> "image/jpeg";
                };
                return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imageBytes);
            }
        } catch (IOException e) {
            log.error("读取头像文件失败: {}", imagePath, e);
        }
        return null;
    }

    private UserVO getUserInfo(Long userId) {
        log.info("user profile querying database, userId={}", userId);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(CommonErrorCode.BIND_USER_NOT_EXISTS);
        }
        UserVO userVo = new UserVO();
        userVo.setId(user.getId());
        userVo.setUsername(user.getUsername());
        userVo.setEmail(user.getEmail());
        userVo.setPhone(user.getPhone());
        userVo.setRole(user.getRole().getId());
        userVo.setSex(user.getSex() == null ? 2 : user.getSex());
        userVo.setImage(convertImageToBase64(user.getImage() != null ? user.getImage() : ""));
        userVo.setFlag(user.getFlag() == null ? 0 : user.getFlag());
        userVo.setStatus(user.getStatus() == null ? 1 : user.getStatus());
        return userVo;
    }

    private String buildUserProfileCacheKey(Long userId) {
        return USER_PROFILE_CACHE_KEY_PREFIX + userId;
    }

    private void cacheUserProfile(UserVO userVo) {
        if (userVo == null || userVo.getId() == null) {
            return;
        }
        boolean cached = redisUtil.set(buildUserProfileCacheKey(userVo.getId()), userVo, USER_PROFILE_CACHE_TTL_SECONDS);
        if (cached) {
            log.info("user profile cache updated, userId={}, ttlSeconds={}", userVo.getId(), USER_PROFILE_CACHE_TTL_SECONDS);
        }
    }

    private void evictUserProfileCache(Long userId) {
        if (userId == null) {
            return;
        }
        redisUtil.del(buildUserProfileCacheKey(userId));
        log.info("user profile cache evicted, userId={}", userId);
    }

    @Override
    public UserVO getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String token = (String) request.getAttribute("token");
        UserVO userVo = getProfile(userId);
        userVo.setToken(token);
        return userVo;
    }

    public UserVO getProfile(Long userId) {
        String cacheKey = buildUserProfileCacheKey(userId);
        UserVO cachedUser = redisUtil.get(cacheKey, UserVO.class);
        if (cachedUser != null) {
            log.info("user profile cache hit, userId={}", userId);
            return cachedUser;
        }

        log.info("user profile cache missed, userId={}", userId);
        UserVO userVo = getUserInfo(userId);
        cacheUserProfile(userVo);
        return userVo;
    }

    @Override
    @Transactional
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
        evictUserProfileCache(userId);
        log.info("用户 {} 头像更新为: {}", userId, filePath);
    }
}
