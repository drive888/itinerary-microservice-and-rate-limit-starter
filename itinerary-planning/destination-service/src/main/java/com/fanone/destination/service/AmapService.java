package com.fanone.destination.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fanone.destination.dto.AmapPOI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class AmapService {

    private static final Logger log = LoggerFactory.getLogger(AmapService.class);

    @Value("${amap.key}")
    private String amapKey;

    @Value("${amap.url}")
    private String amapUrl;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AmapService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Search national scenic spots via Amap API
     * Returns a Map with "list" (POI list) and "total" (total count from Amap)
     */
    public Map<String, Object> searchScenicSpots(String keyword, String city, int pageNum, int pageSize) {
        try {
            // 高德POI搜索单次返回有限(约10-25条)，通过多批次请求+去重获取更多数据
            List<AmapPOI> allPois = new ArrayList<>();
            Set<String> seenIds = new HashSet<>();
            int amapReportedTotal = 0;
            // 最多请求5批，每批用不同offset尝试获取不同数据
            for (int batch = 0; batch < 5; batch++) {
                int offset = batch * 25;
                // 使用JDK HttpClient（基于系统DNS解析，兼容Docker容器环境）
                // 替代WebClient（Netty DNS解析器在Docker容器内无法解析外部域名）
                String url = UriComponentsBuilder.fromHttpUrl(amapUrl)
                        .queryParam("key", amapKey)
                        .queryParam("keywords", keyword != null ? keyword : "旅游景点")
                        .queryParam("types", "110000")
                        .queryParamIfPresent("city", Optional.ofNullable(city).filter(c -> !c.isEmpty()))
                        .queryParam("citylimit", "false")
                        .queryParam("children", "1")
                        .queryParam("extensions", "all")
                        .queryParam("offset", offset)
                        .queryParam("output", "JSON")
                        .toUriString();
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                String responseStr = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();

                if (responseStr == null || responseStr.isEmpty()) break;

                JsonNode root = objectMapper.readTree(responseStr);
                if (root.path("status").asInt(-1) != 1) break;

                if (amapReportedTotal == 0) {
                    amapReportedTotal = root.path("count").asInt(0);
                }

                JsonNode pois = root.path("pois");
                int newCount = 0;
                for (JsonNode poi : pois) {
                    String id = poi.path("id").asText("");
                    if (!seenIds.contains(id)) {
                        seenIds.add(id);
                        AmapPOI item = parsePoiNode(poi);
                        allPois.add(item);
                        newCount++;
                    }
                }

                log.info("Amap batch {}: got {} POIs, {} new (total={})", batch, pois.size(), newCount, allPois.size());
                // 如果这批没有新数据或数据量明显在减少，停止
                if (newCount == 0 || (batch > 0 && newCount < 3)) break;
                // 如果已收集足够多(>100条)，停止
                if (allPois.size() >= 100) break;
            }

            // Build result
            Map<String, Object> result = new HashMap<>();
            result.put("total", allPois.size());

            // Server-side pagination
            int fromIndex = (pageNum - 1) * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, allPois.size());
            if (fromIndex < allPois.size()) {
                result.put("list", allPois.subList(fromIndex, toIndex));
            } else {
                result.put("list", new ArrayList<AmapPOI>());
            }
            return result;

        } catch (Exception e) {
            log.error("Amap API call failed: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("list", new ArrayList<AmapPOI>());
            result.put("total", 0);
            return result;
        }
    }

    /**
     * Get hot cities for quick selection
     */
    public List<String> getHotCities() {
        return List.of(
                "北京", "上海", "广州", "深圳", "杭州",
                "成都", "重庆", "西安", "南京", "苏州",
                "武汉", "长沙", "厦门", "青岛", "大连",
                "三亚", "丽江", "桂林", "拉萨", "哈尔滨"
        );
    }

    /**
     * Get scenic spot categories
     */
    public List<String> getCategories() {
        return List.of(
                "自然风光", "人文古迹", "主题乐园", "博物馆", "宗教场所",
                "公园广场", "风景名胜", "古镇村落", "动物园植物园", "其他"
        );
    }

    /**
     * Parse a single POI JSON node into AmapPOI object
     */
    private AmapPOI parsePoiNode(JsonNode poi) {
        AmapPOI item = new AmapPOI();
        item.setId(poi.path("id").asText(""));
        item.setName(poi.path("name").asText(""));
        item.setType(poi.path("type").asText(""));
        item.setTypeDesc(buildTypeDesc(item.getType()));
        item.setAddress(poi.path("address").asText(""));
        item.setPname(poi.path("pname").asText(""));
        item.setCityname(poi.path("cityname").asText(""));
        item.setAdname(poi.path("adname").asText(""));
        item.setTel(poi.path("tel").asText(""));

        String location = poi.path("location").asText("");
        if (!location.isEmpty()) {
            String[] parts = location.split(",");
            if (parts.length == 2) {
                item.setLocationX(new BigDecimal(parts[0]));
                item.setLocationY(new BigDecimal(parts[1]));
            }
        }

        String bizExtRating = poi.path("biz_ext").path("rating").asText("");
        if (!bizExtRating.isEmpty()) {
            try { item.setRating((int) Double.parseDouble(bizExtRating)); } catch (NumberFormatException ignored) {}
        }

        String bizExtCost = poi.path("biz_ext").path("cost").asText("");
        if (!bizExtCost.isEmpty()) {
            try { item.setCost(Integer.parseInt(bizExtCost)); } catch (NumberFormatException ignored) {}
        }

        JsonNode photos = poi.path("photos");
        if (photos.isArray() && photos.size() > 0) {
            item.setPhotoUrl(photos.get(0).path("url").asText(""));
        }
        return item;
    }

    private String buildTypeDesc(String typeCode) {
        if (typeCode == null) return "景点";
        if (typeCode.contains("110104") || typeCode.contains("110105")) return "景点";
        if (typeCode.contains("110106")) return "公园";
        if (typeCode.contains("110107")) return "动物园/水族馆";
        if (typeCode.contains("110108")) return "植物园";
        if (typeCode.contains("110109")) return "风景区";
        if (typeCode.contains("110110")) return "自然保护区";
        if (typeCode.contains("110113")) return "博物馆";
        if (typeCode.contains("110114")) return "寺庙";
        if (typeCode.contains("110115")) return "教堂";
        if (typeCode.contains("110116")) return "陵墓";
        return "景点";
    }
}
