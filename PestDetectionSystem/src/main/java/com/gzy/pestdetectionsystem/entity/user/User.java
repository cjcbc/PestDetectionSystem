package com.gzy.pestdetectionsystem.entity.user;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "user")
@Schema(description = "用户实体")
public class User {
    // 用户id（唯一）
    @TableId(type = IdType.INPUT)
    private Long id;

    // 用户角色
    private Role role;

    // 用户名
    private String username;

    // 密码
    private String password;

    // 盐值
    private String salt;

    // 邮箱
    private String email;

    // 手机号
    private String phone;

    // 创建时间
    private Long createdTime;

    // 头像
    private String image;

    // 性别 0 男 1 女 2 未知
    private Integer sex;

    // 是否身份认证
    private Boolean isIdentityAuthentication;

    // 状态 0 正常 1 锁定
    private Integer status;

    // 标识 0 普通用户 1 自媒体人 2 大V
    private Integer flag;

    @Getter
    public enum Role {
        ADMIN(0, "管理员"),
        USER(1, "普通用户");

        @EnumValue
        private final int id;
        private final String description;

        Role(int id, String description) {
            this.id = id;
            this.description = description;
        }

    }

}
