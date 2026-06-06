package com.fanone.destination.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fanone.destination.entity.Destination;
import com.fanone.destination.mapper.DestinationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DestinationService extends ServiceImpl<DestinationMapper, Destination> {

    public Page<Destination> listDestinations(int pageNum, int pageSize, String city, String category, String keyword) {
        Page<Destination> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Destination> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Destination::getStatus, 1);

        if (city != null && !city.isEmpty()) {
            wrapper.eq(Destination::getCity, city);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Destination::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Destination::getName, keyword)
                    .or().like(Destination::getDescription, keyword));
        }
        wrapper.orderByDesc(Destination::getRating);

        return this.page(page, wrapper);
    }

    public Destination getDestinationById(Long id) {
        return this.getById(id);
    }

    /**
     * 将高德POI搜索结果保存为本地目的地
     */
    @Transactional(rollbackFor = Exception.class)
    public Destination saveFromAmapPOI(String name, String address, String city,
                                         String province, String category,
                                         BigDecimal locationX, BigDecimal locationY,
                                         String tel) {
        // 查重：同一城市同名目的地不重复保存
        LambdaQueryWrapper<Destination> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Destination::getName, name);
        if (city != null && !city.isEmpty()) {
            wrapper.eq(Destination::getCity, city);
        }
        wrapper.last("LIMIT 1");
        Destination existing = this.getOne(wrapper, false);
        if (existing != null) {
            return existing;
        }

        Destination dest = new Destination();
        dest.setName(name);
        dest.setAddress(address);
        dest.setCity(city);
        dest.setProvince(province);
        dest.setCategory(category != null ? category : "景点");
        dest.setLongitude(locationX);  // 高德返回的是 lng,lat
        dest.setLatitude(locationY);
        dest.setDescription("来自高德地图搜索");
        dest.setStatus(1);
        this.save(dest);
        return dest;
    }
}
