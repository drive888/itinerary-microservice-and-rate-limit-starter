package com.fanone.destination.grpc;

import com.fanone.destination.entity.Destination;
import com.fanone.destination.service.DestinationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * gRPC集成测试 - 启动真实的gRPC Server和Client，测试端到端RPC通信
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GrpcIntegrationTest {

    private static final int TEST_PORT = 19084;
    private static io.grpc.Server grpcServer;
    private static ManagedChannel channel;
    private static DestinationGrpcServiceGrpc.DestinationGrpcServiceBlockingStub blockingStub;
    private static DestinationService mockDestinationService;

    @BeforeAll
    static void setUp() throws Exception {
        // 创建mock的DestinationService
        mockDestinationService = mock(DestinationService.class);

        // 启动gRPC Server
        grpcServer = io.grpc.ServerBuilder.forPort(TEST_PORT)
                .addService(new GrpcDestinationServiceImpl(mockDestinationService))
                .build()
                .start();

        // 创建gRPC Client
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

    // ========== GetDestination RPC 测试 ==========

    @Test
    @Order(1)
    void testGetDestination_FullRoundTrip() {
        // 准备mock数据
        Destination dest = new Destination();
        dest.setId(1L);
        dest.setName("故宫博物院");
        dest.setDescription("北京故宫博物院，世界文化遗产");
        dest.setCity("北京");
        dest.setProvince("北京");
        dest.setCategory("人文古迹");
        dest.setRating(new BigDecimal("4.9"));
        dest.setOpenTime("08:30-17:00");
        dest.setTicketPrice(new BigDecimal("60"));

        when(mockDestinationService.getDestinationById(1L)).thenReturn(dest);

        // 通过gRPC发送请求
        DestinationRequest request = DestinationRequest.newBuilder().setId(1L).build();
        DestinationResponse response = blockingStub.getDestination(request);

        // 验证响应
        assertEquals(1L, response.getId());
        assertEquals("故宫博物院", response.getName());
        assertEquals("北京故宫博物院，世界文化遗产", response.getDescription());
        assertEquals("北京", response.getCity());
        assertEquals("北京", response.getProvince());
        assertEquals("人文古迹", response.getCategory());
        assertEquals(4.9, response.getRating(), 0.01);
        assertEquals("08:30-17:00", response.getOpenTime());
        assertEquals(60.0, response.getTicketPrice(), 0.01);

        verify(mockDestinationService).getDestinationById(1L);
    }

    @Test
    @Order(2)
    void testGetDestination_NotFound() {
        when(mockDestinationService.getDestinationById(999L)).thenReturn(null);

        DestinationRequest request = DestinationRequest.newBuilder().setId(999L).build();

        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () ->
                blockingStub.getDestination(request)
        );

        assertTrue(exception.getStatus().getDescription().contains("目的地不存在"));
        assertTrue(exception.getStatus().getCode().name().equals("NOT_FOUND"));
    }

    @Test
    @Order(3)
    void testGetDestination_MultipleCalls() {
        reset(mockDestinationService);
        for (long id = 1; id <= 3; id++) {
            Destination dest = new Destination();
            dest.setId(id);
            dest.setName("景点" + id);
            dest.setCity("城市" + id);
            dest.setRating(new BigDecimal(String.valueOf(4.0 + id * 0.1)));

            when(mockDestinationService.getDestinationById(id)).thenReturn(dest);
        }

        for (long id = 1; id <= 3; id++) {
            DestinationResponse response = blockingStub.getDestination(
                    DestinationRequest.newBuilder().setId(id).build()
            );
            assertEquals(id, response.getId());
            assertEquals("景点" + id, response.getName());
            assertEquals("城市" + id, response.getCity());
        }

        verify(mockDestinationService, times(3)).getDestinationById(anyLong());
    }

    // ========== ListDestinations RPC 测试 ==========

    @Test
    @Order(4)
    void testListDestinations_FullRoundTrip() {
        Destination d1 = new Destination();
        d1.setId(1L);
        d1.setName("故宫");
        d1.setCity("北京");
        d1.setCategory("人文古迹");
        d1.setRating(new BigDecimal("4.9"));

        Destination d2 = new Destination();
        d2.setId(2L);
        d2.setName("长城");
        d2.setCity("北京");
        d2.setCategory("风景名胜");
        d2.setRating(new BigDecimal("4.8"));

        Page<Destination> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(d1, d2));
        page.setTotal(2);

        when(mockDestinationService.listDestinations(1, 10, null, null, null)).thenReturn(page);

        ListDestinationsRequest request = ListDestinationsRequest.newBuilder()
                .setPageNum(1).setPageSize(10).build();
        DestinationListResponse response = blockingStub.listDestinations(request);

        assertEquals(2, response.getDestinationsCount());
        assertEquals(2, response.getTotal());

        DestinationResponse first = response.getDestinations(0);
        assertEquals(1L, first.getId());
        assertEquals("故宫", first.getName());
        assertEquals("北京", first.getCity());
        assertEquals("人文古迹", first.getCategory());

        DestinationResponse second = response.getDestinations(1);
        assertEquals(2L, second.getId());
        assertEquals("长城", second.getName());
    }

    @Test
    @Order(5)
    void testListDestinations_WithFilters() {
        Destination d1 = new Destination();
        d1.setId(10L);
        d1.setName("西湖");
        d1.setCity("杭州");
        d1.setCategory("风景名胜");

        Page<Destination> page = new Page<>(1, 5);
        page.setRecords(Arrays.asList(d1));
        page.setTotal(1);

        when(mockDestinationService.listDestinations(1, 5, "杭州", "风景名胜", null)).thenReturn(page);

        ListDestinationsRequest request = ListDestinationsRequest.newBuilder()
                .setPageNum(1).setPageSize(5)
                .setCity("杭州").setCategory("风景名胜")
                .build();
        DestinationListResponse response = blockingStub.listDestinations(request);

        assertEquals(1, response.getDestinationsCount());
        assertEquals("西湖", response.getDestinations(0).getName());
        assertEquals("杭州", response.getDestinations(0).getCity());
    }

    @Test
    @Order(6)
    void testListDestinations_Empty() {
        Page<Destination> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);

        when(mockDestinationService.listDestinations(1, 10, "不存在", null, null)).thenReturn(page);

        ListDestinationsRequest request = ListDestinationsRequest.newBuilder()
                .setPageNum(1).setPageSize(10).setCity("不存在").build();
        DestinationListResponse response = blockingStub.listDestinations(request);

        assertEquals(0, response.getDestinationsCount());
        assertEquals(0, response.getTotal());
    }

    @Test
    @Order(7)
    void testListDestinations_Pagination() {
        // 模拟第2页数据
        Destination d1 = new Destination();
        d1.setId(11L);
        d1.setName("景点11");
        d1.setCity("上海");

        Page<Destination> page = new Page<>(2, 10);
        page.setRecords(Arrays.asList(d1));
        page.setTotal(15);

        when(mockDestinationService.listDestinations(2, 10, null, null, null)).thenReturn(page);

        ListDestinationsRequest request = ListDestinationsRequest.newBuilder()
                .setPageNum(2).setPageSize(10).build();
        DestinationListResponse response = blockingStub.listDestinations(request);

        assertEquals(1, response.getDestinationsCount());
        assertEquals(15, response.getTotal());
        assertEquals("景点11", response.getDestinations(0).getName());
    }

    @Test
    @Order(8)
    void testGetDestination_PartialFields() {
        Destination dest = new Destination();
        dest.setId(5L);
        dest.setName("简要景点");

        when(mockDestinationService.getDestinationById(5L)).thenReturn(dest);

        DestinationResponse response = blockingStub.getDestination(
                DestinationRequest.newBuilder().setId(5L).build()
        );

        assertEquals(5L, response.getId());
        assertEquals("简要景点", response.getName());
        // 未设置的字段应返回默认值
        assertEquals("", response.getCity());
        assertEquals("", response.getCategory());
        assertEquals(0.0, response.getRating(), 0.001);
        assertEquals(0.0, response.getTicketPrice(), 0.001);
    }
}
