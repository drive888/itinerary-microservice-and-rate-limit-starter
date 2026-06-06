# 行程规划微服务系统

基于 Spring Cloud + Nacos 的行程规划微服务系统，帮助用户制定行程计划。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 基础框架 |
| Spring Cloud | 2021.0.8 | 微服务框架 |
| Spring Cloud Alibaba | 2021.0.5.0 | 阿里微服务组件 |
| Nacos | 2.2.3 | 服务注册与发现 + 配置中心 |
| Spring Cloud Gateway | - | API网关 |
| OpenFeign | - | 声明式HTTP调用 |
| gRPC | 1.58.0 | RPC通信 |
| MyBatis Plus | 3.5.5 | ORM框架 |
| MySQL | 8.0 | 数据库 |
| Redis | 7 | 缓存 |
| JWT | 0.9.1 | 身份认证 |
| Docker | - | 容器化部署 |

## 项目结构

```
itinerary-planning/
├── pom.xml                          # 父POM
├── docker-compose.yml               # Docker编排
├── Makefile                         # 构建脚本
├── sql/
│   └── init.sql                     # 数据库初始化脚本
├── gateway-service/                 # API网关服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/fanone/gateway/
│       │   ├── GatewayApplication.java
│       │   └── filter/
│       │       └── AuthGlobalFilter.java    # JWT身份校验过滤器
│       └── resources/
│           └── application.yml
├── user-service/                    # 用户管理微服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/fanone/user/
│       │   ├── UserServiceApplication.java
│       │   ├── entity/User.java
│       │   ├── mapper/UserMapper.java
│       │   ├── service/UserService.java
│       │   ├── controller/UserController.java
│       │   ├── config/MybatisPlusConfig.java
│       │   ├── exception/GlobalExceptionHandler.java
│       │   └── util/JwtUtil.java
│       └── resources/
│           └── application.yml
├── itinerary-service/               # 行程规划微服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/fanone/itinerary/
│       │   ├── ItineraryServiceApplication.java
│       │   ├── entity/
│       │   │   ├── Itinerary.java
│       │   │   └── ItineraryDestination.java
│       │   ├── mapper/
│       │   │   ├── ItineraryMapper.java
│       │   │   └── ItineraryDestinationMapper.java
│       │   ├── service/ItineraryService.java
│       │   ├── controller/ItineraryController.java
│       │   ├── feign/DestinationFeignClient.java    # Feign调用目的地服务
│       │   └── config/MybatisPlusConfig.java
│       ├── proto/
│       │   └── destination.proto      # gRPC proto定义
│       └── resources/
│           └── application.yml
└── destination-service/             # 目的地微服务
    ├── pom.xml
    ├── Dockerfile
    └── src/main/
        ├── java/com/fanone/destination/
        │   ├── DestinationServiceApplication.java
        │   ├── entity/Destination.java
        │   ├── mapper/DestinationMapper.java
        │   ├── service/DestinationService.java
        │   ├── controller/DestinationController.java
        │   └── config/MybatisPlusConfig.java
        └── resources/
            └── application.yml
```

## 微服务通信方式

### 1. Spring Cloud OpenFeign（HTTP）

行程规划服务通过 Feign 调用目的地服务获取目的地信息：

```java
@FeignClient(name = "destination-service", path = "/api/destination")
public interface DestinationFeignClient {
    @GetMapping("/{id}")
    Result<?> getDestinationById(@PathVariable("id") Long id);
}
```

### 2. gRPC（RPC）

定义了 `destination.proto`，支持通过 gRPC 进行高效的 RPC 调用：

```protobuf
service DestinationService {
    rpc GetDestination (DestinationRequest) returns (DestinationResponse);
    rpc ListDestinations (ListDestinationsRequest) returns (DestinationListResponse);
}
```

## Nacos 管理界面

启动后访问 http://localhost:8848/nacos

- 默认账号：nacos
- 默认密码：nacos

可在界面中查看：
- **服务列表**：所有注册的微服务实例
- **配置管理**：可动态推送配置到各服务
- **命名空间**：隔离不同环境

## API接口文档

### 用户服务 (user-service:8081)

| 方法 | 路径 | 说明 | 是否需要认证 |
|------|------|------|------------|
| POST | /api/user/register | 用户注册 | 否 |
| POST | /api/user/login | 用户登录 | 否 |
| GET | /api/user/{id} | 获取用户信息 | 否 |
| GET | /api/user/info | 获取当前用户信息 | 是 |

**注册请求：**
```json
{
  "username": "fanone",
  "password": "123456",
  "email": "fanone@example.com"
}
```

**登录响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "userId": 1,
    "username": "fanone",
    "nickname": "fanone"
  }
}
```

### 目的地服务 (destination-service:8083)

| 方法 | 路径 | 说明 | 是否需要认证 |
|------|------|------|------------|
| GET | /api/destination/list | 获取目的地列表 | 否 |
| GET | /api/destination/{id} | 获取目的地详情 | 否 |

**列表查询参数：**
- `pageNum` - 页码（默认1）
- `pageSize` - 每页数量（默认10）
- `city` - 城市筛选
- `category` - 分类筛选（景点/美食/购物）
- `keyword` - 关键词搜索

### 行程服务 (itinerary-service:8082)

| 方法 | 路径 | 说明 | 是否需要认证 |
|------|------|------|------------|
| POST | /api/itinerary/create | 创建行程 | 是 |
| GET | /api/itinerary/list | 获取我的行程列表 | 是 |
| GET | /api/itinerary/{id} | 获取行程详情 | 是 |
| POST | /api/itinerary/{itineraryId}/destination | 添加目的地到行程 | 是 |
| DELETE | /api/itinerary/{itineraryId}/destination/{destinationId} | 从行程中移除目的地 | 是 |
| GET | /api/itinerary/{itineraryId}/destinations | 获取行程中的目的地列表 | 是 |
| PUT | /api/itinerary/{id}/status | 更新行程状态 | 是 |

**创建行程请求：**
```json
{
  "title": "杭州两日游",
  "description": "周末杭州之旅",
  "startDate": "2026-05-01",
  "endDate": "2026-05-02"
}
```

**添加目的地请求：**
```json
{
  "destinationId": 1,
  "visitOrder": 1,
  "note": "早上游览西湖"
}
```

## 快速启动

### 方式一：Docker Compose（推荐）

```bash
# 构建项目
make build

# 启动所有服务
make docker-up
```

### 方式二：本地开发

```bash
# 1. 启动MySQL和Redis
docker-compose up -d mysql redis

# 2. 启动Nacos（单机模式）
docker run -d --name nacos \
  -e MODE=standalone \
  -p 8848:8848 \
  nacos/nacos-server:v2.2.3

# 3. 初始化数据库（自动执行sql/init.sql）

# 4. 启动各微服务
cd user-service && mvn spring-boot:run
cd destination-service && mvn spring-boot:run
cd itinerary-service && mvn spring-boot:run

# 5. 启动网关
cd gateway-service && mvn spring-boot:run
```

## 网关路由

所有请求通过网关(8080)访问：

- `/api/user/**` → user-service
- `/api/itinerary/**` → itinerary-service
- `/api/destination/**` → destination-service

认证方式：在请求头中添加 `Authorization: Bearer <token>`
