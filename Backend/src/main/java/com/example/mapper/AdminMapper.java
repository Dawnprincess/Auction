package com.example.mapper;

import com.example.entity.Admin;
//简单sql可以直接用Select注解，复杂sql可以用xml文件
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AdminMapper {

    List<Admin> selectAll(Admin admin);

    Admin selectById(Integer id);

    List<Admin> selectList(Admin admin);

    void insert(Admin admin);

    void update(Admin admin);

    void deleteById(Integer id);

    @Select("SELECT * FROM admin WHERE account = #{account}")
    Admin selectByAccount(String account);

}
