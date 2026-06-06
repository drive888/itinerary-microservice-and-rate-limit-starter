package com.fanone.itinerary.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 通过Feign调用目的地服务 - Spring Cloud原生通信方式
 */
@FeignClient(name = "destination-service", path = "/api/destination", fallback = DestinationFeignFallback.class)
public interface DestinationFeignClient {

    @GetMapping("/{id}")
    Result<?> getDestinationById(@PathVariable("id") Long id);

    @GetMapping("/list")
    Result<?> listDestinations(@RequestParam("pageNum") int pageNum,
                               @RequestParam("pageSize") int pageSize);

    /**
     * 将高德POI保存为本地目的地
     */
    @PostMapping("/save-from-amap")
    Result<?> saveFromAmap(@RequestBody SaveAmapPOIRequest request);

    /**
     * 高德POI保存请求
     */
    @lombok.Data
    class SaveAmapPOIRequest {
        private String name;
        private String address;
        private String cityname;
        private String pname;
        private String typeDesc;
        private java.math.BigDecimal locationX;
        private java.math.BigDecimal locationY;
        private String tel;
    }

    /**
     * 统一响应结果
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    class Result<T> {
        private int code;
        private String message;
        private T data;
    }
}
