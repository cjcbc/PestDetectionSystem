package com.gzy.pestdetectionsystem.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "返回用户信息")
@AllArgsConstructor
@NoArgsConstructor
public class UserVo {

    //id 唯一标识符
    private Long id;

    //用户名
    private String username;

    //邮箱
    private String email;

    //手机号
    private String phone;

    //JwtToken
    private String token;
}
