package com.example.controller;

import cn.hutool.json.JSONUtil;
import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.Bid;
import com.example.entity.Goods;
import com.example.mapper.BidMapper;
import com.example.service.BidService;
import com.example.service.GoodsService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bid")
public class BidController {

    @Resource
    private BidService bidService;

    @Resource
    private GoodsService goodsService;

    @Resource
    private BidMapper bidMapper;

    /**
     * 提交出价
     */
    @PostMapping("/add")
    public Result add(@RequestBody Bid bid) {
        bidService.addBid(bid);
        return Result.success();
    }

    /**
     * 缴纳保证金
     */
    @PostMapping("/deposit")
    public Result payDeposit(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        System.out.println("收到缴纳保证金请求，参数: " + params); // 【调试日志】
        
        String userHeader = request.getHeader("X-User-Info");
        if (userHeader == null) {
            return Result.error("401", "请先登录");
        }
        
        try {
            Account accountObj = JSONUtil.toBean(userHeader, Account.class);
            String account = accountObj.getAccount();
            System.out.println("当前操作用户: " + account); // 【调试日志】

            // 兼容处理：防止前端传的是字符串类型的数字
            Object goodsIdObj = params.get("goodsId");
            Integer goodsId = goodsIdObj instanceof Integer ? (Integer) goodsIdObj : Integer.parseInt(goodsIdObj.toString());

            String code = bidService.payDeposit(goodsId, account);
            System.out.println("生成竞拍号成功: " + code); // 【调试日志】
            return Result.success(code);
        } catch (Exception e) {
            e.printStackTrace(); // 【重要】在控制台打印具体错误
            return Result.error("500", "缴纳失败: " + e.getMessage());
        }
    }

    /**
     * 获取商品的出价记录列表
     */
    @GetMapping("/list/{goodsId}")
    public Result list(@PathVariable Integer goodsId, HttpServletRequest request) {
        Goods goods = goodsService.selectById(goodsId);
        if (goods == null) {
            return Result.error("404", "商品不存在");
        }

        List<Bid> allBids = bidService.getBidList(goodsId);

        // 【核心修复】获取当前用户的竞拍号（无论什么拍卖类型都要执行）
        String userHeader = request.getHeader("X-User-Info");
        String myCode = null;
        if (userHeader != null) {
            Account accountObj = JSONUtil.toBean(userHeader, Account.class);
            Bid myRecord = bidMapper.selectByGoodsIdAndUser(goodsId, accountObj.getAccount());
            if (myRecord != null && myRecord.getBidderCode() != null) {
                myCode = myRecord.getBidderCode();
            }
        }

        // 【核心】返回一个包含列表和当前用户竞拍号的复合对象
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("myBidderCode", myCode);

        // 核心逻辑：如果是密封式拍卖且尚未结束，隐藏所有出价信息（但依然要返回 myBidderCode）
        if (goods.getAuctionType() == 3 && goods.getStatus() == 1) {
            resultData.put("bids", Collections.emptyList());
        } else {
            // 英式、荷兰式或已结束的密封式，正常返回并过滤掉价格为0的记录
            List<Bid> validBids = allBids.stream()
                    .filter(bid -> bid.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0)
                    .collect(java.util.stream.Collectors.toList());
            resultData.put("bids", validBids);
        }

        return Result.success(resultData);
    }
}
