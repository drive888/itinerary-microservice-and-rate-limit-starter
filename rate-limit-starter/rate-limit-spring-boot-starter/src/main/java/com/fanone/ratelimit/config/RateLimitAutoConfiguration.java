package com.fanone.ratelimit.config;

import com.fanone.ratelimit.algorithm.FixedWindowAlgorithm;
import com.fanone.ratelimit.algorithm.RateLimitAlgorithm;
import com.fanone.ratelimit.algorithm.SlidingWindowAlgorithm;
import com.fanone.ratelimit.algorithm.TokenBucketAlgorithm;
import com.fanone.ratelimit.aspect.RateLimitAspect;
import com.fanone.ratelimit.exception.RateLimitExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 限流 starter 自动装配类
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(RateLimitProperties.class)
@ConditionalOnProperty(prefix = "rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(StringRedisTemplate.class)
    public FixedWindowAlgorithm fixedWindowAlgorithm(StringRedisTemplate redisTemplate) {
        return new FixedWindowAlgorithm(redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(StringRedisTemplate.class)
    public SlidingWindowAlgorithm slidingWindowAlgorithm(StringRedisTemplate redisTemplate) {
        return new SlidingWindowAlgorithm(redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(StringRedisTemplate.class)
    public TokenBucketAlgorithm tokenBucketAlgorithm(StringRedisTemplate redisTemplate) {
        return new TokenBucketAlgorithm(redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(name = "rateLimitAlgorithmMap")
    @ConditionalOnBean(RateLimitAlgorithm.class)
    public Map<String, RateLimitAlgorithm> rateLimitAlgorithmMap(Collection<RateLimitAlgorithm> algorithms) {
        Map<String, RateLimitAlgorithm> map = new HashMap<>();
        for (RateLimitAlgorithm algorithm : algorithms) {
            map.put(algorithm.getName(), algorithm);
            log.info("加载限流算法: {}", algorithm.getName());
        }
        return map;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = "rateLimitAlgorithmMap")
    public RateLimitAspect rateLimitAspect(
            RateLimitProperties properties,
            @Qualifier("rateLimitAlgorithmMap") Map<String, RateLimitAlgorithm> rateLimitAlgorithmMap) {
        return new RateLimitAspect(properties, rateLimitAlgorithmMap);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public RateLimitExceptionHandler rateLimitExceptionHandler() {
        return new RateLimitExceptionHandler();
    }
}
