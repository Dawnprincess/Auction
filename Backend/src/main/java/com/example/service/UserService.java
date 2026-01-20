package com.example.service;

import cn.hutool.core.util.StrUtil;
import com.example.exception.CustomException;
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
        String account=user.getAccount();
        User dbUser = userMapper.selectByAccount(account);
        if (dbUser != null) {
            throw new CustomException("500","账号已存在,请更换");
        }
        //没输入密码，则设置默认密码12345
        if (StrUtil.isBlank(user.getPassword())) {
            user.setPassword("12345");
        }
        //没输入名字,默认为账号
        if(StrUtil.isBlank(user.getName()))
            user.setName("用户" + user.getAccount());
        //没设置权限，默认为普通用户
        if(user.getAccessId() == null)
            user.setAccessId(1);
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

    public User login(User user) {
        String account = user.getAccount();
        String password = user.getPassword();

        User dbUser = userMapper.selectByAccount(account);
        if (dbUser == null){
            throw new CustomException("500","账号不存在");
        }
        if (!dbUser.getPassword().equals(password)) {
            throw new CustomException("500","密码错误");
        }

        return dbUser;
    }

    public void register(User user) {
        this.add(user);
    }
}
