package com.gzy.pestdetectionsystem.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "返回用户信息")
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {

    private String id;
    private int role;
    private String username;
    private String email;
    private String phone;
    private int sex;
    private String image;
    private int status;
    private int flag;

    //JwtToken
    private String token;
}
