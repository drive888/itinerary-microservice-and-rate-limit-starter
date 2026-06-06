package com.fanone.itinerary.controller;

import com.fanone.itinerary.entity.Itinerary;
import com.fanone.itinerary.entity.ItineraryDestination;
import com.fanone.itinerary.service.ItineraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/itinerary")
@RequiredArgsConstructor
public class ItineraryController {

    private final ItineraryService itineraryService;

    @PostMapping("/create")
    public Result<?> createItinerary(@RequestBody Itinerary itinerary,
                                     @RequestHeader("X-User-Id") Long userId) {
        itinerary.setUserId(userId);
        Itinerary created = itineraryService.createItinerary(itinerary);
        return Result.success(created);
    }

    @GetMapping("/list")
    public Result<?> listItineraries(@RequestHeader("X-User-Id") Long userId) {
        List<Itinerary> list = itineraryService.listByUserId(userId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<?> getItinerary(@PathVariable Long id) {
        Itinerary itinerary = itineraryService.getById(id);
        return Result.success(itinerary);
    }

    @PostMapping("/{itineraryId}/destination")
    public Result<?> addDestination(@PathVariable Long itineraryId,
                                    @RequestBody AddDestinationRequest request) {
        itineraryService.addDestination(itineraryId, request.getDestinationId(),
                request.getVisitOrder(), request.getNote());
        return Result.success(null);
    }

    /**
     * 从高德搜索结果添加景点到行程（先保存到本地目的地表，再关联行程）
     */
    @PostMapping("/add-from-amap")
    public Result<?> addFromAmap(@RequestBody AddFromAmapRequest request,
                                  @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Long destId = itineraryService.addFromAmap(
                request.getItineraryId(), request.getName(), request.getAddress(),
                request.getCityname(), request.getPname(), request.getTypeDesc(),
                request.getLocationX(), request.getLocationY(), request.getTel(),
                request.getVisitOrder(), request.getNote());
        return Result.success(destId);
    }

    @DeleteMapping("/{itineraryId}/destination/{destinationId}")
    public Result<?> removeDestination(@PathVariable Long itineraryId,
                                       @PathVariable Long destinationId) {
        itineraryService.removeDestination(itineraryId, destinationId);
        return Result.success(null);
    }

    @GetMapping("/{itineraryId}/destinations")
    public Result<?> listDestinations(@PathVariable Long itineraryId) {
        List<Map<String, Object>> destinations = itineraryService.getItineraryWithDestinations(itineraryId);
        return Result.success(destinations);
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Itinerary itinerary = new Itinerary();
        itinerary.setId(id);
        itinerary.setStatus(status);
        itineraryService.updateById(itinerary);
        return Result.success(null);
    }

    // --- 内部类 ---
    @lombok.Data
    public static class AddDestinationRequest {
        private Long destinationId;
        private Integer visitOrder;
        private String note;
    }

    @lombok.Data
    public static class AddFromAmapRequest {
        private Long itineraryId;
        private String name;
        private String address;
        private String cityname;
        private String pname;
        private String typeDesc;
        private java.math.BigDecimal locationX;
        private java.math.BigDecimal locationY;
        private String tel;
        private Integer visitOrder;
        private String note;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class Result<T> {
        private int code;
        private String message;
        private T data;

        public static <T> Result<T> success(T data) {
            return new Result<>(200, "success", data);
        }

        public static <T> Result<T> error(String message) {
            return new Result<>(500, message, null);
        }
    }
}
