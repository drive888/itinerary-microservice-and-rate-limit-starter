package com.fanone.itinerary.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * gRPC客户端集成测试 - 启动本地gRPC Server，测试Client调用
 * 不依赖Spring Boot容器、Nacos、MySQL
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GrpcClientTest {

    private static final int TEST_PORT = 19085;
    private static io.grpc.Server grpcServer;
    private static ManagedChannel channel;
    private static DestinationGrpcServiceGrpc.DestinationGrpcServiceBlockingStub blockingStub;

    @BeforeAll
    static void setUp() throws Exception {
        // 启动一个简单的gRPC Server（使用内置测试实现）
        grpcServer = io.grpc.ServerBuilder.forPort(TEST_PORT)
                .addService(new TestDestinationGrpcService())
                .build()
                .start();

        channel = ManagedChannelBuilder.forAddress("localhost", TEST_PORT)
                .usePlaintext()
                .build();
        blockingStub = DestinationGrpcServiceGrpc.newBlockingStub(channel);
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (channel != null) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
        if (grpcServer != null) {
            grpcServer.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @Order(1)
    void testGetDestination_ViaClient() {
        DestinationResponse response = blockingStub.getDestination(
                DestinationRequest.newBuilder().setId(1L).build()
        );

        assertEquals(1L, response.getId());
        assertEquals("测试景点", response.getName());
        assertEquals("测试城市", response.getCity());
    }

    @Test
    @Order(2)
    void testListDestinations_ViaClient() {
        DestinationListResponse response = blockingStub.listDestinations(
                ListDestinationsRequest.newBuilder()
                        .setPageNum(1).setPageSize(10).build()
        );

        assertEquals(2, response.getDestinationsCount());
        assertEquals(2, response.getTotal());
        assertEquals("景点A", response.getDestinations(0).getName());
        assertEquals("景点B", response.getDestinations(1).getName());
    }

    @Test
    @Order(3)
    void testGetDestination_NotFound_ViaClient() {
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () ->
                blockingStub.getDestination(
                        DestinationRequest.newBuilder().setId(999L).build()
                )
        );

        assertEquals("NOT_FOUND", exception.getStatus().getCode().name());
    }

    @Test
    @Order(4)
    void testProtoMessageSerialization() throws Exception {
        // 测试protobuf消息的序列化/反序列化
        DestinationRequest request = DestinationRequest.newBuilder()
                .setId(42L)
                .build();

        byte[] bytes = request.toByteArray();
        DestinationRequest parsed = DestinationRequest.parseFrom(bytes);

        assertEquals(42L, parsed.getId());
    }

    @Test
    @Order(5)
    void testListDestinationsRequestBuilder() {
        ListDestinationsRequest request = ListDestinationsRequest.newBuilder()
                .setPageNum(2)
                .setPageSize(20)
                .setCity("杭州")
                .setCategory("景点")
                .build();

        assertEquals(2, request.getPageNum());
        assertEquals(20, request.getPageSize());
        assertEquals("杭州", request.getCity());
        assertEquals("景点", request.getCategory());
    }

    @Test
    @Order(6)
    void testDestinationResponseBuilder() {
        DestinationResponse response = DestinationResponse.newBuilder()
                .setId(10L)
                .setName("测试")
                .setCity("城市")
                .setRating(4.5)
                .setTicketPrice(100.0)
                .build();

        assertEquals(10L, response.getId());
        assertEquals("测试", response.getName());
        assertEquals("城市", response.getCity());
        assertEquals(4.5, response.getRating(), 0.01);
        assertEquals(100.0, response.getTicketPrice(), 0.01);
    }

    /**
     * 测试用的gRPC服务实现 - 返回固定数据
     */
    public static class TestDestinationGrpcService extends DestinationGrpcServiceGrpc.DestinationGrpcServiceImplBase {
        @Override
        public void getDestination(DestinationRequest request, io.grpc.stub.StreamObserver<DestinationResponse> responseObserver) {
            if (request.getId() == 999L) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("目的地不存在")
                        .asRuntimeException());
                return;
            }

            DestinationResponse response = DestinationResponse.newBuilder()
                    .setId(request.getId())
                    .setName("测试景点")
                    .setDescription("测试描述")
                    .setCity("测试城市")
                    .setProvince("测试省份")
                    .setCategory("景点")
                    .setRating(4.5)
                    .setOpenTime("09:00-18:00")
                    .setTicketPrice(50.0)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void listDestinations(ListDestinationsRequest request, io.grpc.stub.StreamObserver<DestinationListResponse> responseObserver) {
            DestinationResponse d1 = DestinationResponse.newBuilder()
                    .setId(1L).setName("景点A").setCity("城市A").setCategory("景点").setRating(4.5)
                    .build();
            DestinationResponse d2 = DestinationResponse.newBuilder()
                    .setId(2L).setName("景点B").setCity("城市B").setCategory("美食").setRating(4.8)
                    .build();

            DestinationListResponse response = DestinationListResponse.newBuilder()
                    .addDestinations(d1)
                    .addDestinations(d2)
                    .setTotal(2)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
