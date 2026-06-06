package com.fanone.itinerary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("itinerary_destinations")
public class ItineraryDestination {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long itineraryId;

    private Long destinationId;

    private Integer visitOrder;

    private LocalDate visitDate;

    private String note;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
