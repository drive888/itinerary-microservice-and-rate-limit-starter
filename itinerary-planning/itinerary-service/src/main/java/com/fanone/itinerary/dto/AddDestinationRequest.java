package com.fanone.itinerary.dto;

import lombok.Data;

@Data
public class AddDestinationRequest {

    private Long destinationId;
    private Integer visitOrder;
    private String note;
}
