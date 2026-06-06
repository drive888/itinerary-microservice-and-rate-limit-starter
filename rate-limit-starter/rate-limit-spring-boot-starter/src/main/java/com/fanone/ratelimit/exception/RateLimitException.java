package com.fanone.ratelimit.exception;

import lombok.Getter;

/**
 * 频率控制异常
 */
@Getter
public class RateLimitException extends RuntimeException {

    private final int statusCode;

    public RateLimitException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
