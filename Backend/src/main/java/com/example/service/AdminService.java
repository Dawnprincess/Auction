package com.example.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.User;
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
    @Resource
    private FileService fileService;

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
        // 获取更新前的用户信息
        Admin oldUser = selectById(admin.getId());

        // 如果有新的头像且与旧头像不同，则删除旧头像
        if (oldUser != null && oldUser.getAvatar() != null &&
                admin.getAvatar() != null && !oldUser.getAvatar().equals(admin.getAvatar())) {
            String oldFileName = fileService.extractFileNameFromUrl(oldUser.getAvatar());
            if (oldFileName != null) {
                String filePath = System.getProperty("user.dir") + "/files/" + oldFileName;
                FileUtil.del(filePath);
            }
        }
        adminMapper.update(admin);
    }

    public void deleteById(Integer id) {
        // 获取用户头像信息
        Admin admin = adminMapper.selectById(id);
        // 检查用户是否存在
        if (admin != null) {
            // 检查头像是否为空
            if (admin.getAvatar() != null && !admin.getAvatar().isEmpty()) {
                String fileName = fileService.extractFileNameFromUrl(admin.getAvatar());
                // 如果有头像，删除头像文件
                if (fileName != null) {
                    String filePath = System.getProperty("user.dir") + "/files/" + fileName;
                    FileUtil.del(filePath);
                }
            }
            adminMapper.deleteById(id);
        } else {
            // 用户不存在的情况下，可以选择抛出异常或记录日志
            throw new RuntimeException("用户不存在，无法删除");
        }
    }
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids)
            this.deleteById(id);
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