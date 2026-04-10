package com.example.controller;

import com.example.common.Result;
import com.example.entity.Bid;
import com.example.service.BidService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bid")
public class BidController {

    @Resource
    private BidService bidService;

    /**
     * 提交出价
     */
    @PostMapping("/add")
    public Result add(@RequestBody Bid bid) {
        bidService.addBid(bid);
        return Result.success();
    }

    /**
     * 获取商品的出价记录列表
     */
    @GetMapping("/list/{goodsId}")
    public Result list(@PathVariable Integer goodsId) {
        List<Bid> list = bidService.getBidList(goodsId);
        return Result.success(list);
    }
}
