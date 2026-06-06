package com.fanone.itinerary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fanone.itinerary.entity.Itinerary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItineraryMapper extends BaseMapper<Itinerary> {
}
