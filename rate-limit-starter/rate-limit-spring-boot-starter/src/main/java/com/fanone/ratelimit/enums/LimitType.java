package com.fanone.ratelimit.enums;

/**
 * 限流相关枚举定义
 */
public final class LimitType {

    private LimitType() {
    }

    /**
     * 限流维度
     */
    public enum Type {
        /** 按 IP 限流 */
        IP,
        /** 按用户限流 */
        USER,
        /** 全局限流 */
        GLOBAL
    }

    /**
     * 时间单位
     */
    public enum TimeUnit {
        SECONDS(java.util.concurrent.TimeUnit.SECONDS),
        MINUTES(java.util.concurrent.TimeUnit.MINUTES),
        HOURS(java.util.concurrent.TimeUnit.HOURS);

        private final java.util.concurrent.TimeUnit delegate;

        TimeUnit(java.util.concurrent.TimeUnit delegate) {
            this.delegate = delegate;
        }

        public long toSeconds(long duration) {
            return delegate.toSeconds(duration);
        }
    }

    /**
     * 限流算法
     */
    public enum Algorithm {
        /** 固定窗口 */
        FIXED_WINDOW,
        /** 滑动窗口 */
        SLIDING_WINDOW,
        /** 令牌桶 */
        TOKEN_BUCKET
    }
}
