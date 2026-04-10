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

        // 3. 校验出价必须高于当前价
        if (bid.getPrice().compareTo(goods.getCurrentPrice()) <= 0) {
            throw new CustomException("500", "出价必须高于当前价格 ¥" + goods.getCurrentPrice());
        }

        // 4. 插入出价记录
        bidMapper.insert(bid);

        // 5. 更新商品当前价格
        Goods updateGoods = new Goods();
        updateGoods.setId(goods.getId());
        updateGoods.setCurrentPrice(bid.getPrice());
        goodsMapper.update(updateGoods);
    }

    /**
     * 获取商品的出价记录列表
     */
    public List<Bid> getBidList(Integer goodsId) {
        return bidMapper.selectByGoodsId(goodsId);
    }
}
