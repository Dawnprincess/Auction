package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.exception.CustomException;
import com.example.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.entity.User;
import com.example.service.UserService;

//对外提供数据接口,返回json格式数据
@RestController
public class WebController {

    //注入UserService
    @Resource
    private UserService userService;
    @Resource
    private AdminService adminService;

    //标注该函数为对外请求接口,可以通过Get请求访问该接口
    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }

    @PostMapping("/login")
    public Result login(@RequestBody Account act) {
        Account dbAct = null;
        if (act.getAccessId() == 0){
            dbAct = adminService.login(act);
        } else if (act.getAccessId() == 1)
            dbAct = userService.login(act);
        return Result.success(dbAct);
    }


    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        userService.register(user);
        return Result.success();
    }
}
