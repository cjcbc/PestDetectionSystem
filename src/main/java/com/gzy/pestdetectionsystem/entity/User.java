package com.gzy.pestdetectionsystem.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "user")
@Schema(description = "用户实体")
public class User {
    //用户id（唯一）
    private Long id;

    //用户名
    private String username;

    //密码
    private String password;

    //盐值
    private String salt;

    //邮箱
    private String email;

    //手机号
    private String phone;

    //创建时间
    private Long createdTime;
}
