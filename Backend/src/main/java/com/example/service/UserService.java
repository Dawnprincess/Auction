package com.example.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
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
    @Resource
    private FileService fileService;

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
        // 获取更新前的用户信息
        User oldUser = selectById(user.getId());
        // 如果有新的头像且与旧头像不同，则删除旧头像
        if (oldUser != null && oldUser.getAvatar() != null &&
                user.getAvatar() != null && !oldUser.getAvatar().equals(user.getAvatar())) {
            String oldFileName = fileService.extractFileNameFromUrl(oldUser.getAvatar());
            if (oldFileName != null) {
                String filePath = System.getProperty("user.dir") + "/files/" + oldFileName;
                FileUtil.del(filePath);
            }
        }
        userMapper.update(user);
    }

    public void deleteById(Integer id) {
        // 获取用户头像信息
        User user = userMapper.selectById(id);
        // 检查用户是否存在
        if (user != null) {
            // 检查头像是否为空
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                String fileName = fileService.extractFileNameFromUrl(user.getAvatar());
                // 如果有头像，删除头像文件
                if (fileName != null) {
                    String filePath = System.getProperty("user.dir") + "/files/" + fileName;
                    FileUtil.del(filePath);
                }
            }
            userMapper.deleteById(id);
        } else {
            // 用户不存在的情况下，可以选择抛出异常或记录日志
            throw new RuntimeException("用户不存在，无法删除");
        }
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

    public Account login(Account act) {
        String account = act.getAccount();
        String password = act.getPassword();

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
