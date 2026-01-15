package com.gzy.pestdetectionsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录参数")
public class LoginDTO {
    //账号
    private String account;

    //密码
    private String password;
}
