package com.example.mapper;

import com.example.entity.Bid;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BidMapper {

    void insert(Bid bid);

    List<Bid> selectByGoodsId(Integer goodsId);
}
