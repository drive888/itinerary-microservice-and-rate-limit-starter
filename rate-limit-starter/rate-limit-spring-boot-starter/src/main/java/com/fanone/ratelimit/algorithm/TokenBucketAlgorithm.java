package com.fanone.ratelimit.algorithm;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

/**
 * 令牌桶限流算法
 * 使用 Lua 脚本保证原子性
 */
public class TokenBucketAlgorithm implements RateLimitAlgorithm {

    /**
     * 返回：0 表示允许，1 表示拒绝
     */
    private static final String TOKEN_BUCKET_SCRIPT =
            "local key = KEYS[1]\n" +
            "local now = tonumber(ARGV[1])\n" +
            "local capacity = tonumber(ARGV[2])\n" +
            "local rate = tonumber(ARGV[3])\n" +
            "local ttl = tonumber(ARGV[4])\n" +
            "local info = redis.call('hmget', key, 'tokens', 'last_time')\n" +
            "local tokens = tonumber(info[1])\n" +
            "local last_time = tonumber(info[2])\n" +
            "if tokens == nil then\n" +
            "    tokens = capacity\n" +
            "    last_time = now\n" +
            "end\n" +
            "local elapsed = math.max(0, (now - last_time) / 1000)\n" +
            "tokens = math.min(capacity, tokens + elapsed * rate)\n" +
            "local allowed = 1\n" +
            "if tokens >= 1 then\n" +
            "    tokens = tokens - 1\n" +
            "    allowed = 0\n" +
            "end\n" +
            "redis.call('hmset', key, 'tokens', tokens, 'last_time', now)\n" +
            "redis.call('expire', key, ttl)\n" +
            "return allowed";

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> redisScript;

    public TokenBucketAlgorithm(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisScript = new DefaultRedisScript<>(TOKEN_BUCKET_SCRIPT, Long.class);
    }

    @Override
    public boolean tryAcquire(String key, long time, long count) {
        String redisKey = "rate_limit:token_bucket:" + key;
        double rate = Math.max((double) count / time, 0.000001D);
        long ttl = Math.max(time * 2, 1);

        Long result = redisTemplate.execute(
                redisScript,
                Collections.singletonList(redisKey),
                String.valueOf(System.currentTimeMillis()),
                String.valueOf(count),
                String.valueOf(rate),
                String.valueOf(ttl)
        );
        return result != null && result == 0L;
    }

    @Override
    public String getName() {
        return "TOKEN_BUCKET";
    }
}
