package com.example.controller;

import com.example.common.Result;
import com.example.entity.Goods;
import com.example.service.GoodsService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private GoodsService goodsService;

    /**
     * 分页查询商品（管理员和普通用户都可以调用，通过 status 过滤）
     */
    @GetMapping("/selectPage")
    public Result selectPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Goods goods) {
        PageInfo<Goods> pageInfo = goodsService.selectPage(pageNum, pageSize, goods);
        return Result.success(pageInfo);
    }

    /**
     * 添加商品（用户发布或管理员添加）
     */
    @PostMapping("/add")
    public Result add(@RequestBody Goods goods) {
        goodsService.add(goods);
        return Result.success();
    }

    /**
     * 更新商品（包括审核通过、下架、修改信息）
     */
    @PutMapping("/update")
    public Result update(@RequestBody Goods goods) {
        goodsService.update(goods);
        return Result.success();
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Integer id) {
        goodsService.deleteById(id);
        return Result.success();
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) {
        return Result.success(goodsService.selectById(id));
    }

    /*
     * 获取商品列表(首页展示)
     */
    @GetMapping("/list")
    public Result list(Goods goods) {
        // 默认只查询 status = 1 (拍卖中) 的商品
        if (goods.getStatus() == null) {
            goods.setStatus(1);
        }
        List<Goods> list = goodsService.selectAll(goods);
        return Result.success(list);
    }

    /*
     * 获取个人商品列表
     */
    @GetMapping("/myList")
    public Result myList(Goods goods) {
        List<Goods> list = goodsService.selectByUserAccount(goods);
        return Result.success(list);}
}
