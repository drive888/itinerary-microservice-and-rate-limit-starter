package com.fanone.destination.grpc;

import com.fanone.destination.service.DestinationService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * gRPC服务器 - 独立端口启动gRPC服务
 */
@Slf4j
@Component
public class GrpcServer {

    @Value("${grpc.server.port:9083}")
    private int grpcPort;

    private final DestinationService destinationService;
    private Server server;

    public GrpcServer(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @PostConstruct
    public void start() throws IOException {
        server = ServerBuilder.forPort(grpcPort)
                .addService(new GrpcDestinationServiceImpl(destinationService))
                .build()
                .start();
        log.info("gRPC server started on port {}", grpcPort);
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
            log.info("gRPC server stopped");
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
