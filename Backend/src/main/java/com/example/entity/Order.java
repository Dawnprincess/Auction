package com.example.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Integer id;
    private String orderNo;
    private Integer goodsId;
    private String sellerAccount;
    private String buyerAccount;
    private BigDecimal price;
    private Integer status;
    private LocalDateTime createTime;
}
