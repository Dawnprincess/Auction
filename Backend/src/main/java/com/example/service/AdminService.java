package com.example.service;

import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.exception.CustomException;
import com.example.mapper.AdminMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import com.example.entity.Admin;
import org.springframework.stereotype.Service;

import java.util.List;

//通过注解将该类注册为Spring Bean
@Service
public class AdminService {
    @Resource
    private AdminMapper adminMapper;

    public void add(Admin admin) {
        String account=admin.getAccount();
        Admin dbAdmin = adminMapper.selectByAccount(account);
        if (dbAdmin != null) {
            throw new CustomException("500","账号已存在,请更换");
        }
        //没输入密码，则设置默认密码12345
        if (StrUtil.isBlank(admin.getPassword())) {
            admin.setPassword("12345");
        }
        //没输入名字,默认为账号
        if(StrUtil.isBlank(admin.getName()))
            admin.setName("用户" + admin.getAccount());
        //没设置权限，默认为普通用户
        if(admin.getAccessId() == null)
            admin.setAccessId(1);
        adminMapper.insert(admin);
    }
    public void update(Admin admin) {
        adminMapper.update(admin);
    }

    public void deleteById(Integer id) {
        adminMapper.deleteById(id);
    }
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            this.deleteById(id);
        }
    }
    public List<Admin> selectAll(Admin admin) {
        return adminMapper.selectAll(admin);
    }
    public Admin selectById(Integer id){
        return adminMapper.selectById(id);
    }
    public List<Admin> selectList(Admin admin){
        return adminMapper.selectList(admin);
    }

    public PageInfo<Admin> selectPage(int pageNum, int pageSize, Admin admin){
        PageHelper.startPage(pageNum, pageSize);
        List<Admin> list = adminMapper.selectAll(admin);
        return PageInfo.of(list);
    }

    public Admin login(Account act) {
        String account = act.getAccount();
        String password = act.getPassword();

        Admin dbAdmin = adminMapper.selectByAccount(account);
        if (dbAdmin == null){
            throw new CustomException("500","账号不存在");
        }
        if (!dbAdmin.getPassword().equals(password)) {
            throw new CustomException("500","密码错误");
        }

        return dbAdmin;
    }
}