package com.gzy.pestdetectionsystem.dto.user;

import com.gzy.pestdetectionsystem.annotation.ContactCheck;
import com.gzy.pestdetectionsystem.annotation.validator.VerifyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户注册参数")
public class RegisterDTO {

    private String username;

    private String password;


    //邮件手机号至少选一
    @ContactCheck(type = VerifyType.PHONE, required = false, message = "手机号格式错误")
    private String phone;
    @ContactCheck(type = VerifyType.EMAIL, required = false, message = "邮箱格式错误")
    private String email;

    private String verificationCodeId;

    private String verificationCode;
}
