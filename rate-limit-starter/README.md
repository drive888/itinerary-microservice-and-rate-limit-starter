# 频率控制 Spring Boot Starter

基于 Spring Boot + Redis 的接口频率控制组件，以注解形式实现。

## 功能特性

- ✅ 自定义 `@RateLimit` 注解，一行代码实现频控
- ✅ 支持多种限流算法：固定窗口、滑动窗口、令牌桶
- ✅ 支持多种限流类型：按IP、按用户、全局限流
- ✅ 支持多频控策略：一个接口同时配置多种限流规则
- ✅ Lua 脚本保证 Redis 操作原子性
- ✅ 支持通过配置文件自定义默认频控参数
- ✅ 支持通过配置文件切换限流算法
- ✅ SPI 机制：允许用户自定义限流算法
- ✅ Spring Boot Starter 自动装配

## 项目结构

```
rate-limit-starter/
├── pom.xml
├── rate-limit-spring-boot-starter/          # Starter核心模块
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/fanone/ratelimit/
│       │   ├── annotation/
│       │   │   ├── RateLimit.java           # 频率控制注解
│       │   │   └── RateLimits.java          # 多频控策略容器注解
│       │   ├── algorithm/
│       │   │   ├── RateLimitAlgorithm.java  # 限流算法接口(SPI)
│       │   │   ├── FixedWindowAlgorithm.java    # 固定窗口算法
│       │   │   ├── SlidingWindowAlgorithm.java  # 滑动窗口算法
│       │   │   └── TokenBucketAlgorithm.java    # 令牌桶算法
│       │   ├── aspect/
│       │   │   └── RateLimitAspect.java     # 频率控制切面
│       │   ├── config/
│       │   │   ├── RateLimitAutoConfiguration.java  # 自动装配类
│       │   │   └── RateLimitProperties.java         # 核心配置类
│       │   ├── enums/
│       │   │   └── LimitType.java           # 枚举定义
│       │   └── exception/
│       │       ├── RateLimitException.java       # 频控异常
│       │       └── RateLimitExceptionHandler.java # 异常处理器
│       └── resources/
│           └── META-INF/
│               ├── spring.factories          # 自动装配配置
│               └── services/
│                   └── com.fanone.ratelimit.algorithm.RateLimitAlgorithm  # SPI配置
└── rate-limit-starter-test/                 # 测试工程
    ├── pom.xml
    ├── docker-compose.yml
    └── src/main/
        ├── java/com/fanone/ratelimit/test/
        │   ├── RateLimitTestApplication.java
        │   └── controller/
        │       └── TestController.java       # 测试接口
        └── resources/
            └── application.yml
```

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.fanone</groupId>
    <artifactId>rate-limit-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置Redis

```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

### 3. 使用注解

```java
@RestController
public class MyController {

    // 5秒内允许3次访问（默认）
    @GetMapping("/api/test")
    @RateLimit(key = "my_api")
    public Result test() {
        return Result.success("ok");
    }
}
```

## 注解参数

### @RateLimit

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| key | String | "rate_limit" | 标识前缀key |
| time | long | 5 | 频控时间范围 |
| timeUnit | TimeUnit | SECONDS | 频控时间单位 |
| count | long | 3 | 单位时间范围内最大访问次数 |
| limitType | Type | IP | 限流类型：IP/USER/GLOBAL |
| algorithm | Algorithm | SLIDING_WINDOW | 限流算法：FIXED_WINDOW/SLIDING_WINDOW/TOKEN_BUCKET |

### @RateLimits（多频控策略）

```java
@GetMapping("/api/multi")
@RateLimits({
    @RateLimit(key = "short", time = 5, count = 3),
    @RateLimit(key = "long", time = 30, count = 10)
})
public Result multi() {
    return Result.success("ok");
}
```

## 配置文件

```yaml
rate-limit:
  enabled: true                      # 是否启用
  default-time: 5                    # 默认时间范围
  default-time-unit: SECONDS         # 默认时间单位
  default-count: 3                   # 默认最大访问次数
  default-limit-type: IP             # 默认限流类型
  default-algorithm: SLIDING_WINDOW  # 默认限流算法
  status-code: 429                   # 限流响应状态码
  message: "请求过于频繁，请稍后再试"  # 限流响应消息
```

## 限流算法说明

### 固定窗口 (FIXED_WINDOW)
- 将时间划分为固定窗口，每个窗口内独立计数
- 实现简单，但存在窗口边界问题
- 适用于要求不严格的场景

### 滑动窗口 (SLIDING_WINDOW) —— 默认
- 基于Redis有序集合实现
- 使用Lua脚本保证原子性
- 精确控制，无边界问题

### 令牌桶 (TOKEN_BUCKET)
- 以恒定速率生成令牌，请求消耗令牌
- 允许突发流量，平滑限流
- 适用于需要处理突发请求的场景

## SPI 自定义限流算法

### 1. 实现接口

```java
public class MyCustomAlgorithm implements RateLimitAlgorithm {
    @Override
    public boolean tryAcquire(String key, long time, long count) {
        // 自定义逻辑
        return true;
    }

    @Override
    public String getName() {
        return "MY_CUSTOM";
    }
}
```

### 2. 注册SPI

在 `META-INF/services/com.fanone.ratelimit.algorithm.RateLimitAlgorithm` 文件中添加：

```
com.example.MyCustomAlgorithm
```

### 3. 使用

```java
@RateLimit(key = "custom", algorithm = LimitType.Algorithm.MY_CUSTOM)
```

> 注意：SPI方式需要在getName()中返回与Algorithm枚举值对应的名称，或者扩展Algorithm枚举。

## 限流响应

当请求被限流时，返回：

```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后再试",
  "data": null
}
```

## 测试工程

```bash
# 启动Redis
cd rate-limit-starter-test && docker-compose up -d

# 启动测试服务
cd rate-limit-starter-test && mvn spring-boot:run

# 测试接口
curl http://localhost:9090/test/default
curl http://localhost:9090/test/fixed
curl http://localhost:9090/test/token-bucket
curl http://localhost:9090/test/multi
```
