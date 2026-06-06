package com.fanone.destination.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SaveAmapRequest {

    private String name;
    private String address;
    private String cityname;
    private String pname;
    private String typeDesc;
    private BigDecimal locationX;
    private BigDecimal locationY;
    private String tel;
}
