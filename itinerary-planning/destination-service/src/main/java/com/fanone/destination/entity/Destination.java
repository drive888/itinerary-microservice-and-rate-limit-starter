package com.fanone.destination.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("destinations")
public class Destination {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String city;

    private String province;

    private String address;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String category;

    private BigDecimal rating;

    private String coverImage;

    private String openTime;

    private BigDecimal ticketPrice;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
