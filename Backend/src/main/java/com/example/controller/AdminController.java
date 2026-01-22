package com.example.controller;

import com.example.common.Result;
import com.example.entity.Admin;
import com.example.service.AdminService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//通过注解@RequestMapping来指定访问路径
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminService adminService;

    /*新增数据*/
    @PostMapping("/add")
    public Result add(@RequestBody Admin admin){
        adminService.add(admin);
        return Result.success();
    }

    /*更新数据*/
    @PutMapping("/update")
    public Result update(@RequestBody Admin admin){
        adminService.update(admin);
        return Result.success();
    }

    /*根据id删除数据*/
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Integer id){
        adminService.deleteById(id);
        return Result.success();
    }
    /*批量删除数据*/
    @DeleteMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids){
        adminService.deleteBatch(ids);
        return Result.success();
    }
    /**
     * 查询所有用户
     */
    //接口路径为/admins/selectAll
    @GetMapping("/selectAll")
    public Result selectAll(Admin admin) {
        List<Admin> admins = adminService.selectAll(admin);
        return Result.success(admins);
    }

    //多个参数在后面再加/即可，如/admins/selectById/{id}/{name} 等
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Admin admin = adminService.selectById(id);
        return Result.success(admin);
    }
    //或者通过@RequestParam注解来获取参数,查询方法 /admins/selectByName?name=xxx(&age=xxx, &sex=xxx等多参数)
/*    @GetMapping("/selectOne")
    public Result selectByName(@RequestParam String name){
        Admin admin = adminService.selectByName(name);
        return Result.success(admin);
    } */

    //也可以通过实体类来接收参数,这样不固定参数数量
    @GetMapping("/selectList")
    public Result selectList(Admin admin) {
        List<Admin> list = adminService.selectList(admin);
        return Result.success(list);
    }

    //分页查询
    @GetMapping("/selectPage")
    public Result selectPage(Admin admin,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Admin> pageInfo = adminService.selectPage(pageNum, pageSize, admin);
        return Result.success(pageInfo);
    }
}
