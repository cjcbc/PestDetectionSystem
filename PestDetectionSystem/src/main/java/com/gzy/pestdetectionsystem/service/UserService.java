package com.gzy.pestdetectionsystem.service;

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
}
