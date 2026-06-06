package com.fanone.ratelimit.config;

import com.fanone.ratelimit.enums.LimitType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 频率控制核心配置类
 * 允许用户通过配置文件自定义默认频控参数
 */
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    /** 是否启用频率控制 */
    private boolean enabled = true;

    /** 默认频控时间范围 */
    private long defaultTime = 5;

    /** 默认频控时间单位 */
    private LimitType.TimeUnit defaultTimeUnit = LimitType.TimeUnit.SECONDS;

    /** 默认单位频控时间范围内最大访问次数 */
    private long defaultCount = 3;

    /** 默认限流类型 */
    private LimitType.Type defaultLimitType = LimitType.Type.IP;

    /** 默认限流算法 */
    private LimitType.Algorithm defaultAlgorithm = LimitType.Algorithm.SLIDING_WINDOW;

    /** 限流时返回的HTTP状态码 */
    private int statusCode = 429;

    /** 限流时返回的消息 */
    private String message = "请求过于频繁，请稍后再试";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getDefaultTime() {
        return defaultTime;
    }

    public void setDefaultTime(long defaultTime) {
        this.defaultTime = defaultTime;
    }

    public LimitType.TimeUnit getDefaultTimeUnit() {
        return defaultTimeUnit;
    }

    public void setDefaultTimeUnit(LimitType.TimeUnit defaultTimeUnit) {
        this.defaultTimeUnit = defaultTimeUnit;
    }

    public long getDefaultCount() {
        return defaultCount;
    }

    public void setDefaultCount(long defaultCount) {
        this.defaultCount = defaultCount;
    }

    public LimitType.Type getDefaultLimitType() {
        return defaultLimitType;
    }

    public void setDefaultLimitType(LimitType.Type defaultLimitType) {
        this.defaultLimitType = defaultLimitType;
    }

    public LimitType.Algorithm getDefaultAlgorithm() {
        return defaultAlgorithm;
    }

    public void setDefaultAlgorithm(LimitType.Algorithm defaultAlgorithm) {
        this.defaultAlgorithm = defaultAlgorithm;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
