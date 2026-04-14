package com.example.service;

import cn.hutool.core.util.IdUtil;
import com.example.entity.Goods;
import com.example.entity.Order;
import com.example.mapper.GoodsMapper;
import com.example.mapper.OrderMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private com.example.mapper.BidMapper bidMapper;

    private String generateOrderNo() {
        return "ORD" + IdUtil.fastSimpleUUID().substring(0, 16).toUpperCase();
    }

    /**
     * 创建订单（通用版，支持指定买家和价格）
     */
    public void createOrder(Integer goodsId, String buyerAccount, BigDecimal price) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setGoodsId(goodsId);
        order.setSellerAccount(goods.getUserAccount());
        order.setBuyerAccount(buyerAccount);
        order.setPrice(price);
        order.setStatus(0); // 0-待支付

        orderMapper.insert(order);
    }

    /**
     * 创建订单（自动结算版，用于英式/密封式结束）
     */
    public void createOrder(Integer goodsId) {
        // 这里可以保留之前的逻辑，通过查询 bid 表找最高价者
        String buyerAccount = getHighestBidder(goodsId);
        Goods goods = goodsMapper.selectById(goodsId);
        createOrder(goodsId, buyerAccount, goods.getCurrentPrice());
    }

    private String getHighestBidder(Integer goodsId) {
        List<com.example.entity.Bid> bids = bidMapper.selectByGoodsId(goodsId);
        return bids.isEmpty() ? null : bids.get(0).getUserAccount();
    }

    public List<Order> getBuyerOrders(String buyerAccount) {
        return orderMapper.selectByBuyerAccount(buyerAccount);
    }

    public List<Order> getSellerOrders(String sellerAccount) {
        return orderMapper.selectBySellerAccount(sellerAccount);
    }

    public void updateOrder(Order order) {
        orderMapper.update(order);
    }

    public Order selectByGoodsId(Integer goodsId) {
        return orderMapper.selectByGoodsId(goodsId);
    }

}
