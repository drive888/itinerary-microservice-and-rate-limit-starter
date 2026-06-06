package com.fanone.ratelimit.algorithm;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 固定窗口限流算法
 */
public class FixedWindowAlgorithm implements RateLimitAlgorithm {

    private final StringRedisTemplate redisTemplate;

    public FixedWindowAlgorithm(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryAcquire(String key, long time, long count) {
        String redisKey = "rate_limit:fixed:" + key;
        Long current = redisTemplate.opsForValue().increment(redisKey);

        if (current != null && current == 1L) {
            redisTemplate.expire(redisKey, time, TimeUnit.SECONDS);
        }

        return current != null && current <= count;
    }

    @Override
    public String getName() {
        return "FIXED_WINDOW";
    }
}
