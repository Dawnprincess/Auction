package com.example.task;

import com.example.entity.Goods;
import com.example.mapper.BidMapper;
import com.example.mapper.GoodsMapper;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class GoodsTask {

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private BidMapper bidMapper;

    @Resource
    private com.example.service.OrderService orderService;

    // 每隔 1 分钟执行一次 (cron 表达式: 秒 分 时 日 月 周)
    @Scheduled(cron = "0/5 * * * * ?")
    public void autoStartAuction() {
        System.out.println("正在检查即将上架的商品...");

        // 1. 查询所有 status = 4 (即将上架) 的商品
        List<Goods> goodsList = goodsMapper.selectByStatus(4);

        LocalDateTime now = LocalDateTime.now();

        for (Goods goods : goodsList) {
            // 2. 如果当前时间已经过了开始时间，且还没结束
            if (goods.getStartTime() != null && !now.isBefore(goods.getStartTime())) {
                if (goods.getEndTime() == null || now.isBefore(goods.getEndTime())) {
                    // 3. 更新状态为 1 (拍卖中)
                    Goods updateGoods = new Goods();
                    updateGoods.setId(goods.getId());
                    updateGoods.setStatus(1);
                    goodsMapper.update(updateGoods);
                    System.out.println("商品 [" + goods.getName() + "] 已自动上架！");
                }
            }
        }
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void handleDutchAuction() {
        System.out.println("正在处理荷兰式拍卖降价...");
        List<Goods> goodsList = goodsMapper.selectByStatus(1); // 只查拍卖中的
        LocalDateTime now = LocalDateTime.now();

        for (Goods goods : goodsList) {
            // 仅处理荷兰式拍卖 (type=2)
            if (goods.getAuctionType() != null && goods.getAuctionType() == 2) {
                if (goods.getStartTime() != null && goods.getPriceChange() != null) {
                    long minutesPassed = java.time.Duration.between(goods.getStartTime(), now).toMinutes();
                    BigDecimal dropAmount = goods.getPriceChange().multiply(new BigDecimal(minutesPassed));
                    BigDecimal newPrice = goods.getStartPrice().subtract(dropAmount);

                    // 价格不能低于保留价
                    if (newPrice.compareTo(goods.getReservePrice()) < 0) {
                        newPrice = goods.getReservePrice();
                    }

                    // 只有当计算出的价格与当前存储价格不同时才更新数据库
                    if (newPrice.compareTo(goods.getCurrentPrice()) != 0) {
                        Goods updateGoods = new Goods();
                        updateGoods.setId(goods.getId());
                        updateGoods.setCurrentPrice(newPrice);
                        goodsMapper.update(updateGoods);
                        System.out.println("荷兰式商品 [" + goods.getName() + "] 价格更新为: " + newPrice);
                    }
                }
            }
        }
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void autoEndAuction() {
        System.out.println("正在检查即将结束的拍卖...");

        List<Goods> goodsList = goodsMapper.selectByStatus(1);

        LocalDateTime now = LocalDateTime.now();

        for (Goods goods : goodsList) {
            if (goods.getEndTime() != null && !now.isBefore(goods.getEndTime())) {
                Goods updateGoods = new Goods();
                updateGoods.setId(goods.getId());
                
                // 区分处理不同拍卖类型
                if (goods.getAuctionType() != null && goods.getAuctionType() == 2) {
                    // 荷兰式拍卖：检查是否有出价记录（即是否有人购买）
                    List<com.example.entity.Bid> bids = bidMapper.selectByGoodsId(goods.getId());
                    if (bids.isEmpty()) {
                        // 没人买，流拍
                        updateGoods.setStatus(3);
                        System.out.println("荷兰式商品 [" + goods.getName() + "] 无人问津，已流拍！");
                    } else {
                        // 有人买了，状态保持为已成交（因为在点击购买时已经改为2并生成订单了）
                        // 这里为了防止意外，可以再次确认状态
                        updateGoods.setStatus(2);
                        System.out.println("荷兰式商品 [" + goods.getName() + "] 已成功售出。");
                    }
                } else if (goods.getAuctionType() != null && goods.getAuctionType() == 3) {
                    // 密封式拍卖：结算逻辑
                    List<com.example.entity.Bid> bids = bidMapper.selectByGoodsId(goods.getId());
                    if (!bids.isEmpty()) {
                        // 简单起见，这里先标记为成交，具体的订单生成和赢家确定可以放在 OrderService 处理
                        updateGoods.setStatus(2);
                        try {
                            orderService.createOrder(goods.getId());
                            System.out.println("密封式商品 [" + goods.getName() + "] 已成交，订单已生成。");
                        } catch (Exception e) {
                            System.err.println("密封式订单生成失败: " + e.getMessage());
                            updateGoods.setStatus(3); // 失败则流拍
                        }
                    } else {
                        updateGoods.setStatus(3);
                        System.out.println("密封式商品 [" + goods.getName() + "] 无人出价，已流拍！");
                    }
                } else {
                    // 英式拍卖：原有逻辑
                    if (goods.getCurrentPrice() != null && 
                        goods.getReservePrice() != null && 
                        goods.getCurrentPrice().compareTo(goods.getReservePrice()) >= 0) {
                        updateGoods.setStatus(2);
                        System.out.println("商品 [" + goods.getName() + "] 已成交！成交价: ¥" + goods.getCurrentPrice());
                        try {
                            orderService.createOrder(goods.getId());
                        } catch (Exception e) {
                            System.err.println("订单生成失败: " + e.getMessage());
                        }
                    } else {
                        updateGoods.setStatus(3);
                        System.out.println("商品 [" + goods.getName() + "] 未达保留价，已流拍！");
                    }
                }
                
                goodsMapper.update(updateGoods);
            }
        }
    }
}
