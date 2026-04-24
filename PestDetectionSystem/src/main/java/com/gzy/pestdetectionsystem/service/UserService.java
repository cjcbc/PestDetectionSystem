package com.gzy.pestdetectionsystem.service;

import com.gzy.pestdetectionsystem.dto.ChangePasswordDTO;
import com.gzy.pestdetectionsystem.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    /**
     * @return 所有用户信息
     */
    List<UserVO> getAllUsers();

    /**
     * 修改用户名
     */
    void updateUsername(Long id, String username);

    void updateSex(Long id, int sex);

    @Deprecated
    UserVO getUserById(Long id);
    UserVO getProfile(HttpServletRequest request);
    UserVO getProfile(Long userId);

    /**
     * 更新用户头像
     * @param userId 用户ID
     * @param file 头像文件
     */
    void updateImage(Long userId, MultipartFile file);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param dto 密码修改信息
     */
    void changePassword(Long userId, ChangePasswordDTO dto);

    /**
     * 禁用用户
     */
    void disableUser(Long operatorId, String userId);

    /**
     * 启用用户
     */
    void enableUser(String userId);

    /**
     * 删除用户
     */
    void deleteUser(String userId);

    void evictProfileCache(Long userId);

    /**
     * 修改用户角色
     */
    void setUserRole(String userId, Integer role);

    void logoutUser(HttpServletRequest request, String token);
}
