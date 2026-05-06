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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class BidService {

    @Resource
    private BidMapper bidMapper;

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private OrderService orderService; // 注入订单服务

    @Resource
    private MessageService messageService;

    /**
     * 提交出价（包含事务）
     */
    @Transactional
    public void addBid(Bid bid) {
        // 1. 校验商品是否存在
        Goods goods = goodsMapper.selectById(bid.getGoodsId());
        if (goods == null || goods.getStatus() != 1) {
            throw new CustomException("500", "商品不存在或不在拍卖中");
        }

        // 2. 【核心逻辑】查找该用户在该商品下是否已有竞拍号
        Bid existingRecord = bidMapper.selectByGoodsIdAndUser(bid.getGoodsId(), bid.getUserAccount());
        
        if (existingRecord != null && existingRecord.getBidderCode() != null) {
            // 如果已经缴纳过保证金，获取旧的竞拍号并赋给新出价
            bid.setBidderCode(existingRecord.getBidderCode());
        } else {
            // 如果没交保证金，根据类型决定是否报错
            throw new CustomException("500", "请先缴纳保证金");
        }

        // 3. 针对不同拍卖类型的特殊处理
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
            // 英式拍卖插入记录
            bidMapper.insert(bid);

            //通知上一个出价人
            List<Bid> allBids = bidMapper.selectByGoodsId(goods.getId());
            if (!allBids.isEmpty()) {
                Bid lastBid = allBids.get(1);
                // 如果上一条出价不是同一个人，说明他被超越了
                if (!lastBid.getUserAccount().equals(bid.getUserAccount())) {
                    messageService.sendMessage(lastBid.getUserAccount(), "竞拍提醒",
                            String.format("您关注的商品 [%s] 当前价已被超越，请前往【商品详情】及时加价！", goods.getName()),
                            1, goods.getId());
                }
            }
            
        } else if (goods.getAuctionType() == 2) {
            // --- 荷兰式拍卖：立即购买 ---
            // 【核心修改】检查是否已经有人真正买过了（即存在 price > 0 的记录）
            List<Bid> bids = bidMapper.selectByGoodsId(goods.getId());
            boolean hasBought = bids.stream().anyMatch(b -> b.getPrice().compareTo(BigDecimal.ZERO) > 0);
            
            if (hasBought) {
                throw new CustomException("500", "该商品已被抢购！");
            }

            // 插入购买记录（此时 bid 对象里已经有 bidderCode 了）
            // 如果用户之前交过保证金，这里会新增一条记录；如果没交过，也会新增
            bid.setCreateTime(LocalDateTime.now());
            bidMapper.insert(bid);

            // 立即生成订单并结束拍卖
            try {
                orderService.createOrder(goods.getId(), bid.getUserAccount(), bid.getPrice());
                Goods updateGoods = new Goods();
                updateGoods.setId(goods.getId());
                updateGoods.setStatus(2); // 已成交
                goodsMapper.update(updateGoods);
            } catch (Exception e) {
                throw new CustomException("500", "订单生成失败: " + e.getMessage());
            }
        } else if (goods.getAuctionType() == 3) {
            // 密封式拍卖：每人只能出价一次
            // 关键校验：查询该用户是否已对该商品出过价
            Bid existingBid = bidMapper.selectByGoodsIdAndUser(bid.getGoodsId(), bid.getUserAccount());
            
            // 【核心修改】如果存在记录，且价格大于0，才判定为已出过价
            // 如果价格是0，说明那是保证金产生的占位记录，允许覆盖更新
            if (existingBid != null && existingBid.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                throw new CustomException("500", "密封拍卖每人限出价一次，您已提交过出价");
            } else{
                // 如果是第一次正式出价（existingBid 为空 或 价格为 0）
                existingBid.setPrice(bid.getPrice());
                bidMapper.update(existingBid);
            }
        }
    }

    /**
     * 获取商品的出价记录列表
     */
    public List<Bid> getBidList(Integer goodsId) {
        return bidMapper.selectByGoodsId(goodsId);
    }

    /**
     * 缴纳保证金并获取竞拍号
     */
    public String payDeposit(Integer goodsId, String userAccount) {
        // 1. 检查商品是否存在且正在拍卖
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null || goods.getStatus() != 1) {
            throw new CustomException("500", "商品不存在或未在拍卖中");
        }

        // 2. 检查是否已经缴纳过（通过查询该商品下该用户是否有 bidder_code）
        Bid existingRecord = bidMapper.selectByGoodsIdAndUser(goodsId, userAccount);
        if (existingRecord != null && existingRecord.getBidderCode() != null) {
            return existingRecord.getBidderCode(); // 已缴纳则直接返回旧号
        }

        // 3. 生成竞拍号 (例如: B-8823)
        String suffix = userAccount.length() > 4 ? userAccount.substring(userAccount.length() - 4) : userAccount;
        int randomNum = new Random().nextInt(9000) + 1000;
        String bidderCode = "B-" + suffix + randomNum;

        // 4. 如果 bid 表里还没记录，先插一条空记录；如果有记录，更新竞拍号
        if (existingRecord == null) {
            Bid newBid = new Bid();
            newBid.setGoodsId(goodsId);
            newBid.setUserAccount(userAccount);
            newBid.setBidderCode(bidderCode);
            newBid.setPrice(BigDecimal.valueOf(0.00)); // 初始价格可以先不设或设为0
            bidMapper.insert(newBid);
        } else {
            Bid updateBid = new Bid();
            updateBid.setId(existingRecord.getId());
            updateBid.setBidderCode(bidderCode);
            bidMapper.update(updateBid);
        }

        return bidderCode;
    }
}
