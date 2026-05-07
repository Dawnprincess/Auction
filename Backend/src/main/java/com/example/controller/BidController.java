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

        // 核心逻辑：如果是密封式拍卖且尚未结束，隐藏所有出价信息
        //结束后可以显示出价信息
        if (goods.getAuctionType() == 3 && goods.getStatus() == 1) {
            // 方案 A：直接返回空列表，让用户以为没人出价
            return Result.success(Collections.emptyList());
        }

        // 【新增】获取当前登录用户，并查找其在该商品下的竞拍号
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
        resultData.put("bids", allBids.stream()
                .filter(bid -> bid.getPrice().compareTo(BigDecimal.ZERO) > 0) // 过滤掉保证金占位记录
                .collect(Collectors.toList()));
        resultData.put("myBidderCode", myCode); // 如果没有号，这里就是 null

        return Result.success(resultData);
    }
}
