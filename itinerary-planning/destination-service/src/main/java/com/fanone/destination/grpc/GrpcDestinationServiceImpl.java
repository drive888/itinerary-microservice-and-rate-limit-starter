package com.fanone.destination.grpc;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fanone.destination.entity.Destination;
import com.fanone.destination.service.DestinationService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * gRPC服务实现 - 处理目的地相关的RPC请求
 * 不加@Component，由GrpcServer手动创建，便于测试
 */
@Slf4j
public class GrpcDestinationServiceImpl extends DestinationGrpcServiceGrpc.DestinationGrpcServiceImplBase {

    private final DestinationService destinationService;

    public GrpcDestinationServiceImpl(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @Override
    public void getDestination(DestinationRequest request, StreamObserver<DestinationResponse> responseObserver) {
        log.info("gRPC - GetDestination request, id={}", request.getId());

        Destination destination = destinationService.getDestinationById(request.getId());
        if (destination == null) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("目的地不存在: id=" + request.getId())
                    .asRuntimeException());
            return;
        }

        DestinationResponse response = buildResponse(destination);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void listDestinations(ListDestinationsRequest request, StreamObserver<DestinationListResponse> responseObserver) {
        log.info("gRPC - ListDestinations request, pageNum={}, pageSize={}, city={}, category={}",
                request.getPageNum(), request.getPageSize(), request.getCity(), request.getCategory());

        int pageNum = request.getPageNum() > 0 ? request.getPageNum() : 1;
        int pageSize = request.getPageSize() > 0 ? request.getPageSize() : 10;

        String city = request.getCity().isEmpty() ? null : request.getCity();
        String category = request.getCategory().isEmpty() ? null : request.getCategory();

        Page<Destination> page = destinationService.listDestinations(pageNum, pageSize, city, category, null);

        List<DestinationResponse> destList = page.getRecords().stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());

        DestinationListResponse response = DestinationListResponse.newBuilder()
                .addAllDestinations(destList)
                .setTotal(page.getTotal())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    DestinationResponse buildResponse(Destination dest) {
        DestinationResponse.Builder builder = DestinationResponse.newBuilder()
                .setId(dest.getId() != null ? dest.getId() : 0)
                .setName(dest.getName() != null ? dest.getName() : "")
                .setDescription(dest.getDescription() != null ? dest.getDescription() : "")
                .setCity(dest.getCity() != null ? dest.getCity() : "")
                .setProvince(dest.getProvince() != null ? dest.getProvince() : "")
                .setCategory(dest.getCategory() != null ? dest.getCategory() : "")
                .setOpenTime(dest.getOpenTime() != null ? dest.getOpenTime() : "");

        if (dest.getRating() != null) {
            builder.setRating(dest.getRating().doubleValue());
        }
        if (dest.getTicketPrice() != null) {
            builder.setTicketPrice(dest.getTicketPrice().doubleValue());
        }

        return builder.build();
    }
}
