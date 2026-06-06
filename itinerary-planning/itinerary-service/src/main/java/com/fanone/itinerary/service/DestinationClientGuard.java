package com.fanone.itinerary.service;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fanone.itinerary.feign.DestinationFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestinationClientGuard {

    public static final String GET_DESTINATION_RESOURCE = "destination-service:getDestinationById";
    public static final String SAVE_AMAP_RESOURCE = "destination-service:saveFromAmap";

    private final DestinationFeignClient destinationFeignClient;

    public DestinationFeignClient.Result<?> getDestinationById(Long destinationId) {
        Entry entry = null;
        try {
            entry = SphU.entry(GET_DESTINATION_RESOURCE);
            DestinationFeignClient.Result<?> result = destinationFeignClient.getDestinationById(destinationId);
            traceDegradedResult(result);
            return result;
        } catch (BlockException e) {
            log.warn("Sentinel blocked resource {}", GET_DESTINATION_RESOURCE);
            return degraded("目的地服务暂时繁忙，请稍后重试");
        } catch (RuntimeException e) {
            Tracer.trace(e);
            throw e;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    public DestinationFeignClient.Result<?> saveFromAmap(DestinationFeignClient.SaveAmapPOIRequest request) {
        Entry entry = null;
        try {
            entry = SphU.entry(SAVE_AMAP_RESOURCE);
            DestinationFeignClient.Result<?> result = destinationFeignClient.saveFromAmap(request);
            traceDegradedResult(result);
            return result;
        } catch (BlockException e) {
            log.warn("Sentinel blocked resource {}", SAVE_AMAP_RESOURCE);
            return degraded("目的地服务暂时繁忙，请稍后重试");
        } catch (RuntimeException e) {
            Tracer.trace(e);
            throw e;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    private DestinationFeignClient.Result<?> degraded(String message) {
        return new DestinationFeignClient.Result<>(503, message, null);
    }

    private void traceDegradedResult(DestinationFeignClient.Result<?> result) {
        if (result != null && result.getCode() >= 500) {
            Tracer.trace(new RuntimeException(result.getMessage()));
        }
    }
}
