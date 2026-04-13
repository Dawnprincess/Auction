package com.example.mapper;

import com.example.entity.Bid;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BidMapper {

    void insert(Bid bid);

    List<Bid> selectByGoodsId(Integer goodsId);

    Bid selectByGoodsIdAndUser(@Param("goodsId") Integer goodsId, @Param("userAccount") String userAccount);
}
