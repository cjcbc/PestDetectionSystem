package com.gzy.pestdetectionsystem.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录参数")
public class LoginDTO {

    private String account;

    @Schema(description = "SM2加密的密码（base64）")
    private String encryptedPassword;

    private String verificationCodeId;

    private String verificationCode;
}
