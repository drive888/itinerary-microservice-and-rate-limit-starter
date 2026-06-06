package com.fanone.itinerary.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign 客户端配置
 */
@Configuration
public class FeignConfig {

    /**
     * 配置 Feign 重试机制
     * 
     * 重试策略：
     * - 初始间隔：100ms
     * - 最大间隔：1秒
     * - 最大尝试次数：3次（首次请求 + 2次重试）
     */
    @Bean
    public Retryer feignRetryer() {
        // 参数说明：
        // period: 初始重试间隔（毫秒）
        // maxPeriod: 最大重试间隔（毫秒）
        // maxAttempts: 最大尝试次数
        return new Retryer.Default(100, TimeUnit.SECONDS.toMillis(1), 3);
    }
}
