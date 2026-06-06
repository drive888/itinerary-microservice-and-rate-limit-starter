package com.fanone.ratelimit.test.controller;

import com.fanone.ratelimit.annotation.RateLimit;
import com.fanone.ratelimit.annotation.RateLimits;
import com.fanone.ratelimit.enums.LimitType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 测试1：默认限流 - 5秒内允许3次访问（滑动窗口，按IP）
     */
    @GetMapping("/default")
    @RateLimit(key = "test_default")
    public Map<String, Object> testDefault() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "请求成功");
        result.put("data", "这是默认限流测试接口");
        return result;
    }

    /**
     * 测试2：固定窗口限流 - 10秒内允许5次访问
     */
    @GetMapping("/fixed")
    @RateLimit(key = "test_fixed", time = 10, count = 5, algorithm = LimitType.Algorithm.FIXED_WINDOW)
    public Map<String, Object> testFixedWindow() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "请求成功");
        result.put("data", "这是固定窗口限流测试接口（10秒内允许5次）");
        return result;
    }

    /**
     * 测试3：令牌桶限流 - 5秒内允许3次访问
     */
    @GetMapping("/token-bucket")
    @RateLimit(key = "test_token_bucket", time = 5, count = 3, algorithm = LimitType.Algorithm.TOKEN_BUCKET)
    public Map<String, Object> testTokenBucket() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "请求成功");
        result.put("data", "这是令牌桶限流测试接口（5秒内允许3次）");
        return result;
    }

    /**
     * 测试4：按用户限流
     */
    @GetMapping("/user")
    @RateLimit(key = "test_user", time = 10, count = 5, limitType = LimitType.Type.USER)
    public Map<String, Object> testUserLimit() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "请求成功");
        result.put("data", "这是按用户限流测试接口（10秒内允许5次）");
        return result;
    }

    /**
     * 测试5：全局限流
     */
    @GetMapping("/global")
    @RateLimit(key = "test_global", time = 10, count = 10, limitType = LimitType.Type.GLOBAL)
    public Map<String, Object> testGlobalLimit() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "请求成功");
        result.put("data", "这是全局限流测试接口（10秒内允许10次）");
        return result;
    }

    /**
     * 测试6：多频控策略 - 5秒内3次 AND 30秒内10次
     */
    @GetMapping("/multi")
    @RateLimits({
            @RateLimit(key = "test_multi_short", time = 5, count = 3),
            @RateLimit(key = "test_multi_long", time = 30, count = 10)
    })
    public Map<String, Object> testMultiLimit() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "请求成功");
        result.put("data", "这是多频控策略测试接口（5秒内3次 AND 30秒内10次）");
        return result;
    }

    /**
     * 测试7：分钟级限流 - 1分钟内允许20次
     */
    @GetMapping("/minute")
    @RateLimit(key = "test_minute", time = 1, count = 20, timeUnit = LimitType.TimeUnit.MINUTES)
    public Map<String, Object> testMinuteLimit() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "请求成功");
        result.put("data", "这是分钟级限流测试接口（1分钟内允许20次）");
        return result;
    }
}
