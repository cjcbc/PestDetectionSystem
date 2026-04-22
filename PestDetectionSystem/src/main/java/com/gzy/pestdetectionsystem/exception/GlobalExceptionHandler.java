package com.gzy.pestdetectionsystem.exception;

import com.gzy.pestdetectionsystem.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException ex) {
        return Result.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return Result.fail(500, "服务器内部错误");
    }
}
