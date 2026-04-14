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

import java.math.BigDecimal;
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
        if (goods.getUserAccount() == null || goods.getUserAccount().isEmpty()) {
            throw new CustomException("500", "发布者账号不能为空");
        }

        // 2. 拍卖类型默认值处理
        if (goods.getAuctionType() == null) {
            goods.setAuctionType(1); // 默认英式
        }
        if (goods.getPriceChange() == null) {
            goods.setPriceChange(new BigDecimal("0"));
        }

        int type = goods.getAuctionType();

        // 3. 针对不同拍卖类型的专项校验
        if (type == 1) { 
            // --- 英式拍卖校验 ---
            if (goods.getReservePrice().compareTo(goods.getStartPrice()) < 0) {
                throw new CustomException("500", "英式拍卖：保留价不能低于起拍价");
            }
            if (goods.getPriceChange().compareTo(BigDecimal.ZERO) <= 0) {
                throw new CustomException("500", "英式拍卖：最小加价幅度必须大于0");
            }
        } else if (type == 2) { 
            // --- 荷兰式拍卖校验 ---
            if (goods.getStartPrice().compareTo(goods.getReservePrice()) < 0) {
                throw new CustomException("500", "荷兰式拍卖：起拍价（最高价）不能低于保留价（最低价）");
            }
            if (goods.getPriceChange().compareTo(BigDecimal.ZERO) <= 0) {
                throw new CustomException("500", "荷兰式拍卖：每分钟降价金额必须大于0");
            }
        } else if (type == 3) { 
            // --- 密封式拍卖校验 ---
            // 强制将梯度设为0，防止前端传错
            goods.setPriceChange(BigDecimal.ZERO); 
            
            if (goods.getReservePrice().compareTo(goods.getStartPrice()) < 0) {
                 throw new CustomException("500", "密封拍卖：保留价不能低于最低出价限制");
            }
        } else {
            throw new CustomException("500", "不支持的拍卖类型");
        }

        // 4. 逻辑校验：时间合理性
        if (goods.getStartTime() != null && goods.getEndTime() != null) {
            if (!goods.getEndTime().isAfter(goods.getStartTime())) {
                throw new CustomException("500", "拍卖结束时间必须晚于开始时间");
            }
            // 禁止发布过去时间的拍卖
            if (goods.getEndTime().isBefore(LocalDateTime.now())) {
                throw new CustomException("500", "拍卖结束时间不能早于当前时间");
            }
        } else {
            throw new CustomException("500", "拍卖开始和结束时间不能为空");
        }
        
        // 5. 用户存在性校验
        User user = userMapper.selectByAccount(goods.getUserAccount());
        if (user == null) {
            throw new CustomException("500", "发布者账号不存在");
        }

        // 6. 初始化字段
        if (goods.getCurrentPrice() == null) {
            // 英式和密封式初始价为起拍价，荷兰式初始价也为起拍价（随后由定时任务递减）
            goods.setCurrentPrice(goods.getStartPrice()); 
        }
        if (goods.getStatus() == null) {
            goods.setStatus(0); // 默认为待审核状态
        }

        goodsMapper.insert(goods);
    }

    /**
     * 更新商品（包含权限校验和业务规则校验）
     * @param goods 前端传来的修改数据
     * @param operatorAccount 当前操作人的账号（从 Header 或 Token 中获取）
     * @param isAdmin 当前操作人是否是管理员
     */
    public void update(Goods goods, String operatorAccount, boolean isAdmin) {
        // 1. 获取数据库中的原始数据
        Goods oldGoods = selectById(goods.getId());
        if (oldGoods == null) {
            throw new CustomException("500", "商品不存在");
        }

        // 2. 权限校验：非管理员只能修改自己的商品
        if (!isAdmin && !oldGoods.getUserAccount().equals(operatorAccount)) {
            throw new CustomException("403", "无权修改他人的商品");
        }

        // 3. 状态机校验：普通用户只能修改“待审核(0)”或“即将上架(4)”的商品
        if (!isAdmin) {
            if (oldGoods.getStatus() != 0 && oldGoods.getStatus() != 4) {
                throw new CustomException("500", "拍卖已开始或已结束，无法修改商品信息");
            }
            
            // 【核心修改】如果用户修改了“即将上架”的商品，强制重置为“待审核”
            if (oldGoods.getStatus() == 4) {
                goods.setStatus(0); 
                System.out.println("用户修改了已审核商品 [" + oldGoods.getName() + "]，状态已重置为待审核");
            }
        }

        // 4. 字段级保护：防止用户通过接口篡改不该改的字段
        if (!isAdmin) {
            // 用户不能手动修改当前价、创建时间等
            goods.setCurrentPrice(null); 
            goods.setCreateTime(null);
            // 注意：status 已经在上面根据逻辑设置了，这里不再置 null
            
            // 如果用户没传某些字段，保留旧值（MyBatis 动态 SQL 会处理 null 不更新）
            if (goods.getName() == null) goods.setName(oldGoods.getName());
            if (goods.getIntro() == null) goods.setIntro(oldGoods.getIntro());
            if (goods.getStartTime() == null) goods.setStartTime(oldGoods.getStartTime());
            if (goods.getEndTime() == null) goods.setEndTime(oldGoods.getEndTime());
        }

        // 5. 业务逻辑校验（复用 add 中的部分逻辑，但要根据状态调整）
        if (goods.getStartPrice() != null && goods.getReservePrice() != null) {
            int type = goods.getAuctionType() != null ? goods.getAuctionType() : oldGoods.getAuctionType();
            if (type == 1 || type == 3) {
                if (goods.getReservePrice().compareTo(goods.getStartPrice()) < 0) {
                    throw new CustomException("500", "保留价不能低于起拍价");
                }
            } else if (type == 2) {
                if (goods.getStartPrice().compareTo(goods.getReservePrice()) < 0) {
                    throw new CustomException("500", "荷兰式拍卖起拍价不能低于保留价");
                }
            }
        }
        
        // 密封式拍卖梯度强制归零
        if (goods.getAuctionType() != null && goods.getAuctionType() == 3) {
            goods.setPriceChange(BigDecimal.ZERO);
        }

        // 6. 处理图片删除逻辑（如果换了新图，删旧图）
        if (oldGoods.getImageUrl() != null && goods.getImageUrl() != null && 
                !oldGoods.getImageUrl().equals(goods.getImageUrl())) {
            String oldFileName = fileService.extractFileNameFromUrl(oldGoods.getImageUrl());
            if (oldFileName != null) {
                FileUtil.del(System.getProperty("user.dir") + "/files/goods/" + oldFileName);
            }
        }

        // 7. 执行更新
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
