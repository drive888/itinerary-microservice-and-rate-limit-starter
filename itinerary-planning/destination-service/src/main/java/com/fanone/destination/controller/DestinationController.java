package com.fanone.destination.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fanone.destination.dto.AmapPOI;
import com.fanone.destination.entity.Destination;
import com.fanone.destination.service.AmapService;
import com.fanone.destination.service.DestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.fanone.ratelimit.annotation.RateLimit;
import com.fanone.ratelimit.enums.LimitType;


import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/destination")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;
    private final AmapService amapService;

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        Page<Destination> page = destinationService.listDestinations(pageNum, pageSize, city, category, keyword);
        return Result.success(page);
    }

    /**
     * 搜索全国旅游景点（通过高德地图API）
     */
    @GetMapping("/search")
    @RateLimit(
            key = "destination_search",
            time = 60,
            count = 20,
            limitType = LimitType.Type.IP,
            algorithm = LimitType.Algorithm.SLIDING_WINDOW
    )
    public Result<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> result = amapService.searchScenicSpots(keyword, city, pageNum, pageSize);
        // Return paginated structure: { list: [...], total: N }
        return Result.success(result);
    }

    /**
     * 获取热门城市列表
     */
    @GetMapping("/search/cities")
    @RateLimit(
            key = "destination_hot_cities",
            time = 10,
            count = 3,
            limitType = LimitType.Type.IP,
            algorithm = LimitType.Algorithm.SLIDING_WINDOW
    )
    public Result<?> hotCities() {
        List<String> cities = amapService.getHotCities();
        return Result.success(cities);
    }

    /**
     * 获取景点分类列表
     */
    @GetMapping("/search/categories")
    public Result<?> categories() {
        List<String> categories = amapService.getCategories();
        return Result.success(categories);
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        Destination destination = destinationService.getDestinationById(id);
        return Result.success(destination);
    }

    /**
     * 将高德搜索结果保存为本地目的地（用于加入行程）
     */
    @PostMapping("/save-from-amap")
    public Result<?> saveFromAmap(@RequestBody SaveAmapRequest request) {
        Destination dest = destinationService.saveFromAmapPOI(
                request.getName(), request.getAddress(), request.getCityname(),
                request.getPname(), request.getTypeDesc(),
                request.getLocationX(), request.getLocationY(), request.getTel());
        return Result.success(dest);
    }

    // --- 请求 DTO ---
    @lombok.Data
    public static class SaveAmapRequest {
        private String name;
        private String address;
        private String cityname;
        private String pname;
        private String typeDesc;
        private java.math.BigDecimal locationX;
        private java.math.BigDecimal locationY;
        private String tel;
    }

    // --- 内部类 ---
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
