package com.fanone.ratelimit.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 频率控制异常处理器
 */
@RestControllerAdvice
public class RateLimitExceptionHandler {

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimitException(RateLimitException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", e.getStatusCode());
        body.put("message", e.getMessage());
        body.put("data", null);
        return ResponseEntity.status(e.getStatusCode()).body(body);
    }
}
