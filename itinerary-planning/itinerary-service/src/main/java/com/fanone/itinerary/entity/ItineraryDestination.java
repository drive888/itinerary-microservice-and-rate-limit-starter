package com.fanone.itinerary.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("itinerary_destinations")
public class ItineraryDestination {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long itineraryId;

    private Long destinationId;

    private Integer visitOrder;

    private LocalDate visitDate;

    private String note;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
