package com.example.mapper;

import com.example.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface OrderMapper {

    void insert(Order order);

    List<Order> selectByBuyerAccount(String buyerAccount);

    List<Order> selectBySellerAccount(String sellerAccount);

    void update(Order order);
    /**
     * 根据商品ID查询订单
     */
    Order selectByGoodsId(Integer goodsId);
}
