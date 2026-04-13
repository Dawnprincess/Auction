package com.example.controller;

import com.example.common.Result;
import com.example.entity.Bid;
import com.example.entity.Goods;
import com.example.service.BidService;
import com.example.service.GoodsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/bid")
public class BidController {

    @Resource
    private BidService bidService;

    @Resource
    private GoodsService goodsService;

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
        Goods goods = goodsService.selectById(goodsId);
        if (goods == null) {
            return Result.error("404", "商品不存在");
        }

        List<Bid> allBids = bidService.getBidList(goodsId);

        // 核心逻辑：如果是密封式拍卖且尚未结束，隐藏所有出价信息
        //结束后可以显示出价信息
        if (goods.getAuctionType() == 3 && goods.getStatus() == 1) {
            // 方案 A：直接返回空列表，让用户以为没人出价
            return Result.success(Collections.emptyList());
        }

        // 英式、荷兰式或已结束的密封式，正常返回
        return Result.success(allBids);
    }
}
