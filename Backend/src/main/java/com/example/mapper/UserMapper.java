package com.example.mapper;

import com.example.entity.User;
//简单sql可以直接用Select注解，复杂sql可以用xml文件
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper {

    List<User> selectAll(User user);

    User selectById(Integer id);

    List<User> selectList(User user);

    void insert(User user);

    void update(User user);

    void deleteById(Integer id);

}
