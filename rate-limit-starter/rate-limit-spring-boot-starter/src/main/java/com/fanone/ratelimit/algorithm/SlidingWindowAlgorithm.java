package com.fanone.ratelimit.algorithm;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

/**
 * 滑动窗口限流算法
 * 使用 Lua 脚本保证原子性
 */
public class SlidingWindowAlgorithm implements RateLimitAlgorithm {

    /**
     * 返回：0 表示允许，1 表示拒绝
     */
    private static final String SLIDING_WINDOW_SCRIPT =
            "local key = KEYS[1]\n" +
            "local now = tonumber(ARGV[1])\n" +
            "local window = tonumber(ARGV[2])\n" +
            "local limit = tonumber(ARGV[3])\n" +
            "redis.call('zremrangebyscore', key, 0, now - window * 1000)\n" +
            "local current = redis.call('zcard', key)\n" +
            "if current < limit then\n" +
            "    redis.call('zadd', key, now, now .. ':' .. redis.call('incr', key .. ':seq'))\n" +
            "    redis.call('expire', key, window)\n" +
            "    redis.call('expire', key .. ':seq', window)\n" +
            "    return 0\n" +
            "else\n" +
            "    return 1\n" +
            "end";

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> redisScript;

    public SlidingWindowAlgorithm(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisScript = new DefaultRedisScript<>(SLIDING_WINDOW_SCRIPT, Long.class);
    }

    @Override
    public boolean tryAcquire(String key, long time, long count) {
        String redisKey = "rate_limit:sliding:" + key;
        Long result = redisTemplate.execute(
                redisScript,
                Collections.singletonList(redisKey),
                String.valueOf(System.currentTimeMillis()),
                String.valueOf(time),
                String.valueOf(count)
        );
        return result != null && result == 0L;
    }

    @Override
    public String getName() {
        return "SLIDING_WINDOW";
    }
}
