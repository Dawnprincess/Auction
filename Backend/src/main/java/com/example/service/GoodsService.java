package com.example.service;

import cn.hutool.core.io.FileUtil;
import com.example.entity.Goods;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.GoodsMapper;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GoodsService {

    @Resource
    private GoodsMapper goodsMapper;
    
    @Resource
    private UserMapper userMapper;

    @Resource
    private FileService fileService;

    /**
     * 新增商品（包含数据校验）
     */
    public void add(Goods goods) {
        // 1. 基础非空校验
        if (goods.getName() == null || goods.getName().isEmpty()) {
            throw new CustomException("500", "商品名称不能为空");
        }
        if (goods.getStartPrice() == null || goods.getReservePrice() == null) {
            throw new CustomException("500", "价格信息不能为空");
        }

        // 2. 逻辑校验：根据拍卖类型判断价格关系
        int auctionType = goods.getAuctionType() != null ? goods.getAuctionType() : 1; // 默认英式
        
        if (auctionType == 1 || auctionType == 3) {
            // 英式/密封式：保留价（底价）不能低于起拍价
            if (goods.getReservePrice().compareTo(goods.getStartPrice()) < 0) {
                throw new CustomException("500", "保留价不能低于起拍价");
            }
        } else if (auctionType == 2) {
            // 荷兰式：起拍价（高价）不能低于保留价（低价）
            if (goods.getStartPrice().compareTo(goods.getReservePrice()) < 0) {
                throw new CustomException("500", "荷兰式拍卖中，起拍价（最高价）不能低于保留价（最低价）");
            }
        }

        // 3. 逻辑校验：时间合理性
        if (goods.getStartTime() != null && goods.getEndTime() != null) {
            if (!goods.getEndTime().isAfter(goods.getStartTime())) {
                throw new CustomException("500", "拍卖结束时间必须晚于开始时间");
            }
            // 可选：禁止发布过去时间的拍卖
            if (goods.getEndTime().isBefore(LocalDateTime.now())) {
                throw new CustomException("500", "拍卖结束时间不能早于当前时间");
            }
        }

        if (goods.getUserAccount() == null || goods.getUserAccount().isEmpty()) {
            throw new CustomException("500", "拍卖人账号不能为空");
        }
        
        User user = userMapper.selectByAccount(goods.getUserAccount());
        if (user == null) {
            throw new CustomException("500", "拍卖人账号不存在");
        }

        if (goods.getCurrentPrice() == null) {
            goods.setCurrentPrice(goods.getStartPrice()); // 初始当前价等于起拍价
        }
        if (goods.getStatus() == null) {
            goods.setStatus(0); // 默认为待审核状态
        }

        goodsMapper.insert(goods);
    }

    /**
     * 更新商品（同样需要校验）
     */
    public void update(Goods goods) {
        // 如果更新了价格或时间，建议再次执行上述校验逻辑
        if (goods.getStartTime() != null && goods.getEndTime() != null) {
             if (!goods.getEndTime().isAfter(goods.getStartTime())) {
                throw new CustomException("500", "拍卖结束时间必须晚于开始时间");
            }
        }
        
        if (goods.getUserAccount() != null && !goods.getUserAccount().isEmpty()) {
            User user = userMapper.selectByAccount(goods.getUserAccount());
            if (user == null) {
                throw new CustomException("500", "拍卖人账号不存在");
            }
        }
        // 获取更新前的商品信息
        Goods oldGoods = selectById(goods.getId());
        // 如果有新的头像且与旧头像不同，则删除旧头像
        if (oldGoods != null && oldGoods.getImageUrl() != null &&
                goods.getImageUrl() != null && !oldGoods.getImageUrl().equals(goods.getImageUrl())) {
            String oldFileName = fileService.extractFileNameFromUrl(oldGoods.getImageUrl());
            if (oldFileName != null) {
                String filePath = System.getProperty("user.dir") + "/files/goods/" + oldFileName;
                FileUtil.del(filePath);
            }
        }
        goodsMapper.update(goods);
    }

    public void deleteById(Integer id) {
        // 获取商品头像信息
        Goods goods = goodsMapper.selectById(id);
        // 检查商品是否存在
        if (goods != null) {
            // 检查头像是否为空
            if (goods.getImageUrl() != null && !goods.getImageUrl().isEmpty()) {
                String oldFileName = fileService.extractFileNameFromUrl(goods.getImageUrl());
                // 如果有头像，删除头像文件
                if (oldFileName != null) {
                    String filePath = System.getProperty("user.dir") + "/files/goods/" + oldFileName;
                    FileUtil.del(filePath);
                }
            }
            goodsMapper.deleteById(id);
        }else{
            throw new CustomException("500", "商品不存在，无法删除");
        }
    }

    public Goods selectById(Integer id) {
        return goodsMapper.selectById(id);
    }

    public PageInfo<Goods> selectPage(int pageNum, int pageSize, Goods goods) {
        PageHelper.startPage(pageNum, pageSize);
        List<Goods> list = goodsMapper.selectAll(goods);
        return PageInfo.of(list);
    }

    public List<Goods> selectAll(Goods goods) {
        return goodsMapper.selectAll(goods);
    }

    public List<Goods> selectByUserAccount(Goods goods) {
        return goodsMapper.selectByUserAccount(goods);
    }
}
