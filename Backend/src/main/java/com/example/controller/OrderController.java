package com.example.controller;

import com.example.common.Result;
import com.example.entity.Order;
import com.example.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    @GetMapping("/buyer/{account}")
    public Result getBuyerOrders(@PathVariable String account) {
        List<Order> orders = orderService.getBuyerOrders(account);
        return Result.success(orders);
    }

    @GetMapping("/seller/{account}")
    public Result getSellerOrders(@PathVariable String account) {
        List<Order> orders = orderService.getSellerOrders(account);
        return Result.success(orders);
    }

    /**
     * 模拟支付（更新订单状态）
     */
    @PutMapping("/pay/{id}")
    public Result pay(@PathVariable Integer id) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(1); // 1-已支付
        // 这里需要在 OrderMapper 中增加 update 方法，或者直接使用通用的 update
        // 为了简单，我们假设 OrderMapper 有 update 方法
        orderService.updateOrder(order); 
        return Result.success();
    }

    /**
     * 根据商品ID查询订单详情（管理员查看用）
     */
    @GetMapping("/detail")
    public Result getDetail(Integer goodsId) {
        if (goodsId == null) {
            return Result.error("400", "商品ID不能为空");
        }
        Order order = orderService.selectByGoodsId(goodsId);
        if (order == null) {
            return Result.error("404", "该商品暂无成交订单");
        }
        return Result.success(order);
    }
}
