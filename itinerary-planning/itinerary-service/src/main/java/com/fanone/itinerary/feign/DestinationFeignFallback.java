package com.fanone.itinerary.feign;

import org.springframework.stereotype.Component;

@Component
public class DestinationFeignFallback implements DestinationFeignClient {

    private static final String MESSAGE = "目的地服务暂时繁忙，请稍后重试";

    @Override
    public Result<?> getDestinationById(Long id) {
        return degraded();
    }

    @Override
    public Result<?> listDestinations(int pageNum, int pageSize) {
        return degraded();
    }

    @Override
    public Result<?> saveFromAmap(SaveAmapPOIRequest request) {
        return degraded();
    }

    private Result<?> degraded() {
        return new Result<>(503, MESSAGE, null);
    }
}
