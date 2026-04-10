package com.example.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Bid {
    private Integer id;
    private Integer goodsId;
    private String userAccount;
    private BigDecimal price;
    private LocalDateTime createTime;
}
