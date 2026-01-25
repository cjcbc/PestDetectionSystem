package com.gzy.pestdetectionsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户注册参数")
public class RegisterDTO {

    private String username;

    private String password;


    //邮件手机号至少选一
    private String email;
    private String phone;
}

