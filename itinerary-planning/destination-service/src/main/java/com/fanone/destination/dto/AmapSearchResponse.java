package com.fanone.destination.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmapSearchResponse {
    private int status;
    private int infoCode;
    private String info;
    private List<AmapPOI> pois;
    private int count;
}
