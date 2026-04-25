package com.gzy.pestdetectionsystem.assembler.user;

import com.gzy.pestdetectionsystem.entity.user.User;
import com.gzy.pestdetectionsystem.vo.user.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Slf4j
@Component
public class UserAssembler {

    /**
     * 完整信息（含头像 base64、role、sex、flag、status），用于个人资料场景
     */
    public UserVO toProfileVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId().toString());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole().getId());
        vo.setSex(user.getSex() == null ? 2 : user.getSex());
        vo.setImage(convertImageToBase64(user.getImage() != null ? user.getImage() : ""));
        vo.setFlag(user.getFlag() == null ? 0 : user.getFlag());
        vo.setStatus(user.getStatus() == null ? 1 : user.getStatus());
        return vo;
    }

    /**
     * 列表信息（不含头像 base64，避免数据过大），用于管理员列表场景
     */
    public UserVO toListVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId().toString());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole().getId());
        vo.setSex(user.getSex() == null ? 2 : user.getSex());
        vo.setFlag(user.getFlag() == null ? 0 : user.getFlag());
        vo.setStatus(user.getStatus() == null ? 1 : user.getStatus());
        return vo;
    }

    /**
     * 基础信息（仅 id、username、email、phone），用于 getUserById 等轻量场景
     */
    public UserVO toBasicVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId().toString());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        return vo;
    }

    private String convertImageToBase64(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        try {
            Path path = Paths.get(imagePath);
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
}
