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
    USER_BANNED(40013,"用户已被禁用"),

    // 绑定 4002x
    BIND_PARAM_INVALID(40020,"绑定参数不合法"),
    BIND_USER_NOT_EXISTS(40021,"用户不存在"),
    BIND_TYPE_NOT_SUPPORTED(40022,"不支持的绑定类型"),
    BIND_PHONE_SAME(40023,"新手机号与原手机号一致"),
    BIND_PHONE_EXISTS(40024,"手机号已被其他账号绑定"),
    BIND_EMAIL_SAME(40025,"新邮箱与原邮箱一致"),
    BIND_EMAIL_EXISTS(40026,"邮箱已被其他账号绑定"),

    // LLM 4003x
    LLM_PARAM_INVALID(40030, "LLM 请求参数不合法"),
    LLM_API_KEY_MISSING(40031, "未配置 LLM API Key"),
    LLM_CALL_FAILED(40032, "调用 LLM 服务失败"),
    LLM_RESPONSE_EMPTY(40033, "LLM 返回为空"),

    // Chat 4004x
    CHAT_SESSION_NOT_FOUND(40040, "对话会话不存在或已删除"),
    CHAT_QUOTA_EXCEEDED(40041, "今日对话次数已达上限"),
    CHAT_MESSAGE_EMPTY(40042, "消息内容不能为空"),

    // User 4005x
    USER_AVATAR_EMPTY(40050, "头像文件不能为空"),
    USER_AVATAR_TYPE_INVALID(40051, "只能上传图片文件"),
    USER_AVATAR_TOO_LARGE(40052, "图片大小不能超过5MB"),

    // 修改密码 4006x
    CHANGE_PASSWORD_PARAM_INVALID(40060, "密码修改参数不合法"),
    CHANGE_PASSWORD_OLD_PASSWORD_WRONG(40061, "原密码错误"),
    CHANGE_PASSWORD_SAME_AS_OLD(40062, "新密码不能与原密码相同"),
    CHANGE_PASSWORD_NOT_MATCH(40063, "两次输入的新密码不一致"),

    // 验证码 4007x
    VERIFICATION_CODE_INVALID(40070, "验证码错误"),
    VERIFICATION_CODE_REQUIRED(40071, "验证码不能为空"),
    VERIFICATION_CODE_STORE_FAILED(40072, "验证码生成失败"),

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
