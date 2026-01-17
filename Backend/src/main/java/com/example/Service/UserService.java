package com.example.Service;

import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import com.example.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

//通过注解将该类注册为Spring Bean
@Service
public class UserService {
    @Resource
    private UserMapper userMapper;

    public void add(User user) {
        userMapper.insert(user);
    }
    public void update(User user) {
        userMapper.update(user);
    }

    public void deleteById(Integer id) {
        userMapper.deleteById(id);
    }
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            this.deleteById(id);
        }
    }
    public List<User> selectAll(User user) {
        return userMapper.selectAll(user);
    }
    public User selectById(Integer id){
        return userMapper.selectById(id);
    }
    public List<User> selectList(User user){
        return userMapper.selectList(user);
    }

    public PageInfo<User> selectPage(int pageNum, int pageSize, User user){
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userMapper.selectAll(user);
        return PageInfo.of(list);
    }
}
