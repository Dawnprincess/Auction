package com.example.mapper;

import com.example.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface GoodsMapper {
    void insert(Goods goods);
    void update(Goods goods);
    void deleteById(Integer id);
    Goods selectById(Integer id);
    List<Goods> selectAll(Goods goods);
}
