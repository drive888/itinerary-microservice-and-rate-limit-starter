	docker-compose down
	docker-compose up -d
.PHONY: proto build clean

# 生成gRPC代码
proto:
	mvn compile -pl itinerary-service

# 编译所有模块
build:
	mvn clean package -DskipTests

# 编译指定模块
build-user:
	mvn clean package -pl user-service -DskipTests

build-itinerary:
	mvn clean package -pl itinerary-service -DskipTests

build-destination:
	mvn clean package -pl destination-service -DskipTests

build-gateway:
	mvn clean package -pl gateway-service -DskipTests

	docker compose build
docker-build:
	docker-compose build
	docker compose up -d
docker-up:
	docker-compose up -d
	docker compose down
docker-down:
	docker-compose down

# 清理
clean:
	mvn clean
