package com.fanone.itinerary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fanone.itinerary.entity.Itinerary;
import com.fanone.itinerary.entity.ItineraryDestination;
import com.fanone.itinerary.feign.DestinationFeignClient;
import com.fanone.itinerary.mapper.ItineraryDestinationMapper;
import com.fanone.itinerary.mapper.ItineraryMapper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ItineraryService extends ServiceImpl<ItineraryMapper, Itinerary> {

    private final ItineraryDestinationMapper itineraryDestinationMapper;
    private final DestinationClientGuard destinationClientGuard;

    public List<Itinerary> listByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<Itinerary>()
                .eq(Itinerary::getUserId, userId)
                .orderByDesc(Itinerary::getCreatedAt));
    }

    @Transactional
    public Itinerary createItinerary(Itinerary itinerary) {
        itinerary.setStatus(0);
        this.save(itinerary);
        return itinerary;
    }

    @Transactional
    public void addDestination(Long itineraryId, Long destinationId, Integer visitOrder, String note) {
        // 通过Feign调用目的地服务，验证目的地是否存在
        DestinationFeignClient.Result<?> result = destinationClientGuard.getDestinationById(destinationId);
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new RuntimeException(result != null && result.getCode() == 503 ? result.getMessage() : "目的地不存在");
        }

        ItineraryDestination id = new ItineraryDestination();
        id.setItineraryId(itineraryId);
        id.setDestinationId(destinationId);
        id.setVisitOrder(visitOrder != null ? visitOrder : 0);
        id.setNote(note);
        itineraryDestinationMapper.insert(id);
    }

    @Transactional
    public void removeDestination(Long itineraryId, Long destinationId) {
        itineraryDestinationMapper.delete(new LambdaQueryWrapper<ItineraryDestination>()
                .eq(ItineraryDestination::getItineraryId, itineraryId)
                .eq(ItineraryDestination::getDestinationId, destinationId));
    }

    public List<ItineraryDestination> listDestinations(Long itineraryId) {
        return itineraryDestinationMapper.selectList(new LambdaQueryWrapper<ItineraryDestination>()
                .eq(ItineraryDestination::getItineraryId, itineraryId)
                .orderByAsc(ItineraryDestination::getVisitOrder));
    }

    /**
     * 从高德POI添加景点到行程：先通过Feign保存为本地目的地，再关联行程
     */
    @GlobalTransactional(name = "add-from-amap-tx", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Long addFromAmap(Long itineraryId, String name, String address,
                             String cityname, String pname, String typeDesc,
                             BigDecimal locationX, BigDecimal locationY,
                             String tel, Integer visitOrder, String note) {
        // 1. 通过Feign调用destination服务保存高德POI为本地目的地
        DestinationFeignClient.SaveAmapPOIRequest poiRequest = new DestinationFeignClient.SaveAmapPOIRequest();
        poiRequest.setName(name);
        poiRequest.setAddress(address);
        poiRequest.setCityname(cityname);
        poiRequest.setPname(pname);
        poiRequest.setTypeDesc(typeDesc);
        poiRequest.setLocationX(locationX);
        poiRequest.setLocationY(locationY);
        poiRequest.setTel(tel);

        DestinationFeignClient.Result<?> saveResult = destinationClientGuard.saveFromAmap(poiRequest);
        if (saveResult == null || saveResult.getCode() != 200 || saveResult.getData() == null) {
            throw new RuntimeException(saveResult != null && saveResult.getCode() == 503 ? saveResult.getMessage() : "保存景点失败");
        }

        // 2. 从保存结果中提取目的地ID
        Long destinationId = null;
        if (saveResult.getData() instanceof Map) {
            Object idObj = ((Map<?, ?>) saveResult.getData()).get("id");
            if (idObj instanceof Number) {
                destinationId = ((Number) idObj).longValue();
            }
        }
        if (destinationId == null) {
            throw new RuntimeException("获取目的地ID失败");
        }

        // 3. 直接关联到行程（跳过验证，因为目的地刚刚保存成功）
        ItineraryDestination id = new ItineraryDestination();
        id.setItineraryId(itineraryId);
        id.setDestinationId(destinationId);
        id.setVisitOrder(visitOrder != null ? visitOrder : 0);
        id.setNote(note);
        itineraryDestinationMapper.insert(id);

        return destinationId;
    }

    /**
     * 获取行程中所有目的地的详细信息
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getItineraryWithDestinations(Long itineraryId) {
        List<ItineraryDestination> idList = listDestinations(itineraryId);
        return idList.stream().map(id -> {
            DestinationFeignClient.Result<?> result = destinationClientGuard.getDestinationById(id.getDestinationId());
            if (result != null && result.getCode() == 200 && result.getData() instanceof Map) {
                Map<String, Object> destInfo = (Map<String, Object>) result.getData();
                destInfo.put("visitOrder", id.getVisitOrder());
                destInfo.put("visitDate", id.getVisitDate());
                destInfo.put("note", id.getNote());
                return destInfo;
            }
            return null;
        }).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toList());
    }
}
