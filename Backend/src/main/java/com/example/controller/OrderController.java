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
}
