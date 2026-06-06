package com.fanone.itinerary.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddFromAmapRequest {

    private Long itineraryId;
    private String name;
    private String address;
    private String cityname;
    private String pname;
    private String typeDesc;
    private BigDecimal locationX;
    private BigDecimal locationY;
    private String tel;
    private Integer visitOrder;
    private String note;
}
