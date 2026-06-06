# Itinerary Microservices and Rate Limit Starter

This repository contains two related Java projects:

1. `itinerary-planning`: a Spring Cloud itinerary planning microservice system.
2. `rate-limit-starter`: a reusable Redis-based rate limiting Spring Boot Starter.

## Project Structure

```text
.
├── itinerary-planning
│   ├── gateway-service
│   ├── user-service
│   ├── itinerary-service
│   ├── destination-service
│   ├── frontend
│   ├── sql
│   └── docker-compose.yml
├── rate-limit-starter
│   ├── rate-limit-spring-boot-starter
│   └── rate-limit-starter-test
└── docs-assets
```

## Itinerary Planning Microservices

The `itinerary-planning` project implements an itinerary planning system based on Spring Cloud.

Main modules:

- `gateway-service`: unified gateway, routing, CORS, and JWT authentication.
- `user-service`: user registration, login, JWT generation, and user information APIs.
- `destination-service`: destination list/search APIs, Amap POI integration, gRPC server, and rate limit integration.
- `itinerary-service`: itinerary creation, itinerary-destination association, OpenFeign calls, gRPC client, Seata distributed transaction, and Sentinel degradation.
- `frontend`: simple local web UI for testing the microservice system.

Key technologies:

- Spring Boot
- Spring Cloud Gateway
- Nacos
- OpenFeign
- gRPC
- MyBatis Plus
- MySQL
- Redis
- JWT
- Seata
- Sentinel
- Docker Compose

## Rate Limit Starter

The `rate-limit-starter` project provides a reusable rate limiting starter.

Main features:

- `@RateLimit` annotation for single rate limit rules.
- `@RateLimits` annotation for multiple rate limit rules on one API.
- AOP-based request interception.
- Redis-based rate limit state storage.
- Fixed window, sliding window, and token bucket algorithms.
- Lua scripts for atomic Redis operations.
- Custom exception handling with HTTP 429 responses.

## Quick Start

### Build the rate limit starter

```bash
cd rate-limit-starter
mvn clean install -DskipTests
```

### Build the itinerary microservice system

```bash
cd itinerary-planning
mvn clean package -DskipTests
```

### Start infrastructure

Start MySQL, Seata, and other required infrastructure according to `itinerary-planning/docker-compose.yml`.

```bash
cd itinerary-planning
docker compose up -d mysql seata-server
```

If Nacos is not already running locally, start Nacos separately and make sure it listens on:

```text
localhost:8848
```

### Start services locally

```bash
cd itinerary-planning

java -jar user-service/target/user-service-1.0.0-SNAPSHOT.jar
java -jar destination-service/target/destination-service-1.0.0-SNAPSHOT.jar
java -jar itinerary-service/target/itinerary-service-1.0.0-SNAPSHOT.jar
java -jar gateway-service/target/gateway-service-1.0.0-SNAPSHOT.jar
```

Default gateway address:

```text
http://localhost:8080
```

## Environment Variables

The Amap API key is read from:

```text
AMAP_KEY
```

Example:

```bash
export AMAP_KEY=your-amap-key
```

## Main API Examples

Register:

```bash
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test001","password":"123456","email":"test001@qq.com"}'
```

Login:

```bash
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test001","password":"123456"}'
```

List destinations:

```bash
curl "http://localhost:8080/api/destination/list?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer <token>"
```

Create itinerary:

```bash
curl -X POST http://localhost:8080/api/itinerary/create \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"杭州周末两日游","description":"西湖和灵隐寺路线","startDate":"2026-06-07","endDate":"2026-06-08"}'
```

Fast rate limit test:

```bash
for i in {1..5}; do
  printf "Request %s: " "$i"
  curl -s "http://localhost:8080/api/destination/search/cities"
  printf "\n"
done
```

The hot cities API is configured with `@RateLimit` and allows 3 requests per 10 seconds from the same IP. The 4th request should return HTTP 429.

## Notes

- Build outputs, logs, IDE files, and OS temporary files are ignored by `.gitignore`.
- Do not commit real API keys or production secrets.
- Database initialization SQL is located in `itinerary-planning/sql/init.sql`.

