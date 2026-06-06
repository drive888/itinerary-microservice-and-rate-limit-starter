package com.fanone.ratelimit.aspect;

import cn.hutool.core.util.StrUtil;
import com.fanone.ratelimit.annotation.RateLimit;
import com.fanone.ratelimit.annotation.RateLimits;
import com.fanone.ratelimit.algorithm.RateLimitAlgorithm;
import com.fanone.ratelimit.config.RateLimitProperties;
import com.fanone.ratelimit.exception.RateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 频率控制切面
 * 对标注了 @RateLimit 或 @RateLimits 注解的方法进行频控增强
 */
@Slf4j
@Aspect
public class RateLimitAspect {

    private static final String DEFAULT_ALGORITHM = "SLIDING_WINDOW";

    private final RateLimitProperties properties;
    private final Map<String, RateLimitAlgorithm> algorithmMap;

    public RateLimitAspect(RateLimitProperties properties, Map<String, RateLimitAlgorithm> algorithmMap) {
        this.properties = properties;
        this.algorithmMap = algorithmMap;
    }

    @Around("@annotation(com.fanone.ratelimit.annotation.RateLimit) || @annotation(com.fanone.ratelimit.annotation.RateLimits)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.isEnabled()) {
            return joinPoint.proceed();
        }

        Method method = resolveMethod(joinPoint);

        RateLimit rateLimit = AnnotationUtils.findAnnotation(method, RateLimit.class);
        if (rateLimit != null) {
            checkRateLimit(rateLimit, joinPoint);
        }

        RateLimits rateLimits = AnnotationUtils.findAnnotation(method, RateLimits.class);
        if (rateLimits != null) {
            for (RateLimit limit : rateLimits.value()) {
                checkRateLimit(limit, joinPoint);
            }
        }

        return joinPoint.proceed();
    }

    private Method resolveMethod(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            method = joinPoint.getTarget().getClass().getMethod(signature.getName(), method.getParameterTypes());
        }
        return method;
    }

    private void checkRateLimit(RateLimit rateLimit, ProceedingJoinPoint joinPoint) {
        String key = buildKey(rateLimit, joinPoint);
        long time = Math.max(1, rateLimit.timeUnit().toSeconds(rateLimit.time()));
        long count = Math.max(1, rateLimit.count());
        String algorithmName = rateLimit.algorithm().name();

        RateLimitAlgorithm algorithm = algorithmMap.get(algorithmName);
        if (algorithm == null) {
            log.warn("未找到限流算法: {}, 使用默认滑动窗口算法", algorithmName);
            algorithm = algorithmMap.get(DEFAULT_ALGORITHM);
        }
        if (algorithm == null) {
            throw new IllegalStateException("未配置可用的限流算法: " + algorithmName);
        }

        boolean allowed = algorithm.tryAcquire(key, time, count);
        if (!allowed) {
            log.warn("请求被限流, key: {}, time: {}s, count: {}", key, time, count);
            throw new RateLimitException(properties.getStatusCode(), properties.getMessage());
        }
    }

    /**
     * 构建限流 key
     * 格式: prefix:className:methodName:limitType:identifier:window
     */
    private String buildKey(RateLimit rateLimit, ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(rateLimit.key());
        keyBuilder.append(":").append(className);
        keyBuilder.append(":").append(methodName);

        switch (rateLimit.limitType()) {
            case IP:
                keyBuilder.append(":ip:").append(getClientIp());
                break;
            case USER:
                keyBuilder.append(":user:").append(getUserId());
                break;
            case GLOBAL:
                keyBuilder.append(":global");
                break;
            default:
                keyBuilder.append(":unknown");
                break;
        }

        keyBuilder.append(":").append(rateLimit.timeUnit().toSeconds(rateLimit.time())).append("s");
        return keyBuilder.toString();
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return StrUtil.blankToDefault(ip, "unknown");
    }

    private String getUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "anonymous";
        }
        HttpServletRequest request = attributes.getRequest();
        String userId = request.getHeader("X-User-Id");
        return StrUtil.blankToDefault(userId, "anonymous");
    }
}
