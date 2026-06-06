package com.fanone.itinerary.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * gRPC客户端 - 连接destination-service的gRPC服务
 */
@Slf4j
@Component
public class GrpcClient {

    @Value("${grpc.client.destination-service.host:localhost}")
    private String host;

    @Value("${grpc.client.destination-service.port:9083}")
    private int port;

    private ManagedChannel channel;
    private DestinationGrpcServiceGrpc.DestinationGrpcServiceBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = DestinationGrpcServiceGrpc.newBlockingStub(channel);
        log.info("gRPC client connected to {}:{}", host, port);
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            log.info("gRPC client disconnected");
        }
    }

    public DestinationResponse getDestination(long id) {
        log.info("gRPC client - GetDestination, id={}", id);
        DestinationRequest request = DestinationRequest.newBuilder().setId(id).build();
        return blockingStub.getDestination(request);
    }

    public DestinationListResponse listDestinations(int pageNum, int pageSize, String city, String category) {
        log.info("gRPC client - ListDestinations, pageNum={}, pageSize={}", pageNum, pageSize);
        ListDestinationsRequest.Builder builder = ListDestinationsRequest.newBuilder()
                .setPageNum(pageNum)
                .setPageSize(pageSize);
        if (city != null) builder.setCity(city);
        if (category != null) builder.setCategory(category);
        return blockingStub.listDestinations(builder.build());
    }
}
