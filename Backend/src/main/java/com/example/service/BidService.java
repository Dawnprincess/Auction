package com.example.service;

import com.example.entity.Bid;
import com.example.entity.Goods;
import com.example.exception.CustomException;
import com.example.mapper.BidMapper;
import com.example.mapper.GoodsMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BidService {

    @Resource
    private BidMapper bidMapper;

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private OrderService orderService; // 注入订单服务

    /**
     * 提交出价（包含事务）
     */
    @Transactional
    public void addBid(Bid bid) {
        // 1. 校验商品是否存在
        Goods goods = goodsMapper.selectById(bid.getGoodsId());
        if (goods == null) {
            throw new CustomException("500", "商品不存在");
        }

        // 2. 校验商品状态是否为拍卖中
        if (goods.getStatus() != 1) {
            throw new CustomException("500", "该商品不在拍卖中，无法出价");
        }

        // 针对不同拍卖类型的校验
        if (goods.getAuctionType() == 1) {
            // 英式拍卖：必须高于当前价 + 梯度
            BigDecimal minNextPrice = goods.getCurrentPrice().add(goods.getPriceChange() != null ? goods.getPriceChange() : new BigDecimal("1"));
            if (bid.getPrice().compareTo(minNextPrice) < 0) {
                throw new CustomException("500", "加价幅度不能小于 ¥" + goods.getPriceChange());
            }
            // 更新当前价
            Goods updateGoods = new Goods();
            updateGoods.setId(goods.getId());
            updateGoods.setCurrentPrice(bid.getPrice());
            goodsMapper.update(updateGoods);
            
        } else if (goods.getAuctionType() == 2) {
            // 荷兰式拍卖：直接成交
            // 1. 插入出价记录
            bidMapper.insert(bid);
            // 2. 生成订单
            orderService.createOrder(goods.getId(), bid.getUserAccount(), bid.getPrice());
            // 3. 更新商品状态为已成交
            Goods updateGoods = new Goods();
            updateGoods.setId(goods.getId());
            updateGoods.setStatus(2); // 2-已成交
            goodsMapper.update(updateGoods);
            return; // 荷兰式直接返回，不执行后面的通用逻辑
        }

        // 英式拍卖插入记录
        bidMapper.insert(bid);
    }

    /**
     * 获取商品的出价记录列表
     */
    public List<Bid> getBidList(Integer goodsId) {
        return bidMapper.selectByGoodsId(goodsId);
    }
}
