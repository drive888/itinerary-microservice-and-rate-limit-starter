package com.fanone.ratelimit.algorithm;

/**
 * 限流算法接口 - SPI机制
 * 允许用户自定义实现限流算法
 */
public interface RateLimitAlgorithm {

    /**
     * 尝试获取访问许可
     *
     * @param key     限流key
     * @param time    时间范围
     * @param count   最大访问次数
     * @return 是否允许访问
     */
    boolean tryAcquire(String key, long time, long count);

    /**
     * 获取算法名称
     */
    String getName();
}
