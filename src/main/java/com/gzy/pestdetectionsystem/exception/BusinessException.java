package com.gzy.pestdetectionsystem.exception;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class BusinessException extends RuntimeException{
    private final Integer code;
    private final String message;

    // 枚举传入错误码自动匹配消息
    public BusinessException(@NonNull CommonErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    // 枚举传入错误码和自定义消息
    public BusinessException(@NonNull CommonErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.message = message;
    }

    // 自定义错误码和消息
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }


}
