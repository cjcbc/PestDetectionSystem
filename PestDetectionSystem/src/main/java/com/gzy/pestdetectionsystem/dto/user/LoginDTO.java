package com.gzy.pestdetectionsystem.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录参数")
public class LoginDTO {

    private String account;

    private String password;

    @Schema(description = "SM2加密的密码（base64），与password二选一，优先用encryptedPassword")
    private String encryptedPassword;

    private String verificationCodeId;

    private String verificationCode;
}
