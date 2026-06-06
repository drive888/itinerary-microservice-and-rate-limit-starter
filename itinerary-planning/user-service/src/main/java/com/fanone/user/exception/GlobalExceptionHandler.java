package com.fanone.user.exception;

import com.fanone.user.controller.UserController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public UserController.Result<?> handleRuntimeException(RuntimeException e) {
        return UserController.Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public UserController.Result<?> handleException(Exception e) {
        return UserController.Result.error("服务器内部错误");
    }
}
