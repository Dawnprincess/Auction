package com.example.task;

import com.example.entity.Goods;
import com.example.mapper.GoodsMapper;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class GoodsTask {

    @Resource
    private GoodsMapper goodsMapper;

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
    public void autoEndAuction() {
        System.out.println("正在检查即将结束的拍卖...");

        List<Goods> goodsList = goodsMapper.selectByStatus(1);

        LocalDateTime now = LocalDateTime.now();

        for (Goods goods : goodsList) {
            if (goods.getEndTime() != null && !now.isBefore(goods.getEndTime())) {
                Goods updateGoods = new Goods();
                updateGoods.setId(goods.getId());
                
                if (goods.getCurrentPrice() != null && 
                    goods.getReservePrice() != null && 
                    goods.getCurrentPrice().compareTo(goods.getReservePrice()) >= 0) {
                    updateGoods.setStatus(2);
                    System.out.println("商品 [" + goods.getName() + "] 已成交！成交价: ¥" + goods.getCurrentPrice());
                    
                    try {
                        orderService.createOrder(goods.getId());
                        System.out.println("订单已自动生成");
                    } catch (Exception e) {
                        System.err.println("订单生成失败: " + e.getMessage());
                    }
                } else {
                    updateGoods.setStatus(3);
                    System.out.println("商品 [" + goods.getName() + "] 已流拍！");
                }
                
                goodsMapper.update(updateGoods);
            }
        }
    }
}
