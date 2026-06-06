package com.fanone.destination.grpc;

import com.fanone.destination.entity.Destination;
import com.fanone.destination.service.DestinationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * gRPC服务端单元测试 - 直接测试GrpcDestinationServiceImpl逻辑
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GrpcDestinationServiceTest {

    private DestinationService destinationService;
    private GrpcDestinationServiceImpl grpcService;

    @BeforeEach
    void setUp() {
        destinationService = mock(DestinationService.class);
        grpcService = new GrpcDestinationServiceImpl(destinationService);
    }

    // ========== buildResponse 测试 ==========

    @Test
    @Order(1)
    void testBuildResponse_AllFields() {
        Destination dest = new Destination();
        dest.setId(1L);
        dest.setName("故宫博物院");
        dest.setDescription("北京故宫博物院");
        dest.setCity("北京");
        dest.setProvince("北京");
        dest.setCategory("人文古迹");
        dest.setRating(new BigDecimal("4.9"));
        dest.setOpenTime("08:30-17:00");
        dest.setTicketPrice(new BigDecimal("60"));

        DestinationResponse response = grpcService.buildResponse(dest);

        assertEquals(1L, response.getId());
        assertEquals("故宫博物院", response.getName());
        assertEquals("北京故宫博物院", response.getDescription());
        assertEquals("北京", response.getCity());
        assertEquals("北京", response.getProvince());
        assertEquals("人文古迹", response.getCategory());
        assertEquals(4.9, response.getRating(), 0.01);
        assertEquals("08:30-17:00", response.getOpenTime());
        assertEquals(60.0, response.getTicketPrice(), 0.01);
    }

    @Test
    @Order(2)
    void testBuildResponse_NullFields() {
        Destination dest = new Destination();
        dest.setId(2L);
        dest.setName("测试景点");

        DestinationResponse response = grpcService.buildResponse(dest);

        assertEquals(2L, response.getId());
        assertEquals("测试景点", response.getName());
        assertEquals("", response.getCity());
        assertEquals("", response.getProvince());
        assertEquals("", response.getCategory());
        assertEquals("", response.getDescription());
        assertEquals("", response.getOpenTime());
        assertEquals(0.0, response.getRating(), 0.001);
        assertEquals(0.0, response.getTicketPrice(), 0.001);
    }

    @Test
    @Order(3)
    void testBuildResponse_NullRatingAndPrice() {
        Destination dest = new Destination();
        dest.setId(3L);
        dest.setName("无评分景点");
        dest.setRating(null);
        dest.setTicketPrice(null);

        DestinationResponse response = grpcService.buildResponse(dest);

        assertEquals(3L, response.getId());
        assertEquals(0.0, response.getRating(), 0.001);
        assertEquals(0.0, response.getTicketPrice(), 0.001);
    }

    // ========== getDestination 测试 ==========

    @Test
    @Order(4)
    void testGetDestination_Success() {
        Destination dest = new Destination();
        dest.setId(1L);
        dest.setName("故宫博物院");
        dest.setCity("北京");
        dest.setRating(new BigDecimal("4.9"));

        when(destinationService.getDestinationById(1L)).thenReturn(dest);

        io.grpc.stub.StreamObserver<DestinationResponse> observer = mock(io.grpc.stub.StreamObserver.class);
        grpcService.getDestination(
                DestinationRequest.newBuilder().setId(1L).build(),
                observer
        );

        verify(observer).onNext(argThat(response ->
                response.getId() == 1L &&
                "故宫博物院".equals(response.getName()) &&
                "北京".equals(response.getCity())
        ));
        verify(observer).onCompleted();
    }

    @Test
    @Order(5)
    void testGetDestination_NotFound() {
        when(destinationService.getDestinationById(999L)).thenReturn(null);

        io.grpc.stub.StreamObserver<DestinationResponse> observer = mock(io.grpc.stub.StreamObserver.class);
        grpcService.getDestination(
                DestinationRequest.newBuilder().setId(999L).build(),
                observer
        );

        verify(observer, never()).onNext(any());
        verify(observer, never()).onCompleted();
        verify(observer).onError(argThat(throwable ->
                throwable instanceof StatusRuntimeException
        ));
    }

    // ========== listDestinations 测试 ==========

    @Test
    @Order(6)
    void testListDestinations_Success() {
        Destination d1 = new Destination();
        d1.setId(1L);
        d1.setName("故宫");
        d1.setCity("北京");
        d1.setRating(new BigDecimal("4.9"));

        Destination d2 = new Destination();
        d2.setId(2L);
        d2.setName("长城");
        d2.setCity("北京");
        d2.setRating(new BigDecimal("4.8"));

        Page<Destination> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(d1, d2));
        page.setTotal(2);

        when(destinationService.listDestinations(1, 10, null, null, null)).thenReturn(page);

        io.grpc.stub.StreamObserver<DestinationListResponse> observer = mock(io.grpc.stub.StreamObserver.class);
        grpcService.listDestinations(
                ListDestinationsRequest.newBuilder().setPageNum(1).setPageSize(10).build(),
                observer
        );

        verify(observer).onNext(argThat(response ->
                response.getDestinationsCount() == 2 &&
                response.getTotal() == 2 &&
                "故宫".equals(response.getDestinations(0).getName()) &&
                "长城".equals(response.getDestinations(1).getName())
        ));
        verify(observer).onCompleted();
    }

    @Test
    @Order(7)
    void testListDestinations_EmptyResult() {
        Page<Destination> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);

        when(destinationService.listDestinations(1, 10, "不存在的城市", null, null)).thenReturn(page);

        io.grpc.stub.StreamObserver<DestinationListResponse> observer = mock(io.grpc.stub.StreamObserver.class);
        grpcService.listDestinations(
                ListDestinationsRequest.newBuilder()
                        .setPageNum(1).setPageSize(10)
                        .setCity("不存在的城市").build(),
                observer
        );

        verify(observer).onNext(argThat(response ->
                response.getDestinationsCount() == 0 &&
                response.getTotal() == 0
        ));
        verify(observer).onCompleted();
    }

    @Test
    @Order(8)
    void testListDestinations_WithCityFilter() {
        Destination d1 = new Destination();
        d1.setId(1L);
        d1.setName("西湖");
        d1.setCity("杭州");
        d1.setRating(new BigDecimal("4.8"));

        Page<Destination> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(d1));
        page.setTotal(1);

        when(destinationService.listDestinations(1, 10, "杭州", null, null)).thenReturn(page);

        io.grpc.stub.StreamObserver<DestinationListResponse> observer = mock(io.grpc.stub.StreamObserver.class);
        grpcService.listDestinations(
                ListDestinationsRequest.newBuilder()
                        .setPageNum(1).setPageSize(10)
                        .setCity("杭州").build(),
                observer
        );

        verify(observer).onNext(argThat(response ->
                response.getDestinationsCount() == 1 &&
                "杭州".equals(response.getDestinations(0).getCity())
        ));
        verify(observer).onCompleted();
    }

    @Test
    @Order(9)
    void testListDestinations_DefaultPageParams() {
        Page<Destination> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);

        when(destinationService.listDestinations(1, 10, null, null, null)).thenReturn(page);

        io.grpc.stub.StreamObserver<DestinationListResponse> observer = mock(io.grpc.stub.StreamObserver.class);
        // 传入pageNum=0, pageSize=0，应该使用默认值
        grpcService.listDestinations(
                ListDestinationsRequest.newBuilder().setPageNum(0).setPageSize(0).build(),
                observer
        );

        verify(observer).onNext(any());
        verify(observer).onCompleted();
    }
}
