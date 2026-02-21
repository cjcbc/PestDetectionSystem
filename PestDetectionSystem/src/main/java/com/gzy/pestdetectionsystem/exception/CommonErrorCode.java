package com.gzy.pestdetectionsystem.exception;

import lombok.Getter;

@Getter
public enum CommonErrorCode {

    // 通用认证
    UNAUTHORIZED(401, "未登录或登录过期"),
    FORBIDDEN(403,"无访问权限"),
    TOKEN_INVALID(40101,"token 无效"),
    TOKEN_EXPIRED(40102,"token 已过期"),

    // 注册 4000x
    REGISTER_PARAM_INVALID(40000,"注册参数不合法"),
    REGISTER_ACCOUNT_REQUIRED(40001, "手机号和邮箱至少填写一个"),
    REGISTER_PASSWORD_REQUIRED(40002,"密码不能为空"),
    REGISTER_PHONE_EXISTS(40003,"手机号已注册"),
    REGISTER_EMAIL_EXISTS(40004,"邮箱已注册"),

    // 登录 4001x
    LOGIN_PARAM_INVALID(40010,"登录参数不合法"),
    LOGIN_ACCOUNT_NOT_FOUND(40011,"账号不存在"),
    LOGIN_PASSWORD_ERROR(40012,"账号或密码错误"),

    // 绑定 4002x
    BIND_PARAM_INVALID(40020,"绑定参数不合法"),
    BIND_USER_NOT_EXISTS(40021,"用户不存在"),
    BIND_TYPE_NOT_SUPPORTED(40022,"不支持的绑定类型"),
    BIND_PHONE_SAME(40023,"新手机号与原手机号一致"),
    BIND_PHONE_EXISTS(40024,"手机号已被其他账号绑定"),
    BIND_EMAIL_SAME(40025,"新邮箱与原邮箱一致"),
    BIND_EMAIL_EXISTS(40026,"邮箱已被其他账号绑定"),



    UNKNOWN(999999, "未知错误");


    private final int code;

    private final String message;

    CommonErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public static CommonErrorCode fromCode(int code) {
        for (CommonErrorCode errorCode : CommonErrorCode.values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return UNKNOWN;
    }
}