package com.gzy.pestdetectionsystem.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordDTO {
    // 原密码
    private String oldPassword;
    
    // 新密码
    private String newPassword;
    
    // 确认密码
    private String confirmPassword;
}
