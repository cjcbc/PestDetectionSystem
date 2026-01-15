package com.gzy.pestdetectionsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户注册参数")
public class RegisterDTO {

    private String username;

    private String password;

    private String email;

    private String phone;
}

