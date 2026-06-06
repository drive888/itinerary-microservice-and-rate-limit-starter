package com.fanone.ratelimit.annotation;

import com.fanone.ratelimit.enums.LimitType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 频率控制注解
 * 用于标记需要进行频率控制的接口
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 标识前缀key
     */
    String key() default "rate_limit";

    /**
     * 频控时间范围（单位由timeUnit指定）
     */
    long time() default 5;

    /**
     * 频控时间单位
     */
    LimitType.TimeUnit timeUnit() default LimitType.TimeUnit.SECONDS;

    /**
     * 单位频控时间范围内最大访问次数
     */
    long count() default 3;

    /**
     * 限流类型：IP / 用户 / 全局
     */
    LimitType.Type limitType() default LimitType.Type.IP;

    /**
     * 限流算法：固定窗口 / 滑动窗口 / 令牌桶
     * 默认使用滑动窗口
     */
    LimitType.Algorithm algorithm() default LimitType.Algorithm.SLIDING_WINDOW;
}
