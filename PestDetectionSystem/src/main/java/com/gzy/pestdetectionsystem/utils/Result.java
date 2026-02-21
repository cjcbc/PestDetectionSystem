package com.gzy.pestdetectionsystem.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static Result<?> ok(String msg) {
        return new Result<>(200, msg, null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }

    // 失败返回，自定义状态码和信息
    public static Result<?> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}
