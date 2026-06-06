package com.fanone.destination.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmapPOI {
    private String id;
    private String name;
    private String type;
    private String typeDesc;
    private String address;
    private String pname;       // 省份
    private String cityname;    // 城市
    private String adname;      // 区/县
    private BigDecimal locationX; // 经度
    private BigDecimal locationY; // 纬度
    private String tel;
    private Integer rating;     // 评分
    private Integer cost;       // 人均消费
    private String photoUrl;    // 首张图片
}
