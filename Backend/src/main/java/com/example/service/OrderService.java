package com.example.service;

import cn.hutool.core.util.IdUtil;
import com.example.entity.Goods;
import com.example.entity.Order;
import com.example.mapper.GoodsMapper;
import com.example.mapper.OrderMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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

    public void createOrder(Integer goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }

        String buyerAccount = getHighestBidder(goodsId);
        if (buyerAccount == null || buyerAccount.isEmpty()) {
            throw new RuntimeException("没有出价记录，无法生成订单");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setGoodsId(goodsId);
        order.setSellerAccount(goods.getUserAccount());
        order.setBuyerAccount(buyerAccount);
        order.setPrice(goods.getCurrentPrice());
        order.setStatus(0);

        orderMapper.insert(order);
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
}
