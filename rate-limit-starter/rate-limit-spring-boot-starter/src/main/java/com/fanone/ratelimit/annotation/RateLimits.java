package com.fanone.ratelimit.annotation;

import java.lang.annotation.*;

/**
 * 支持多个频控策略的容器注解
 * 允许某个接口拥有多种频控策略（如5s内3次、30s内10次）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimits {

    RateLimit[] value();
}
