package com.example.controller;

import com.example.common.Result;
import com.example.exception.CustomException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//对外提供数据接口,返回json格式数据
@RestController
public class WebController {

    //标注该函数为对外请求接口,可以通过Get请求访问该接口
    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }

    @GetMapping("/int")
    public Integer number(){
        return 10;
    }

    @GetMapping("/count")
    public Result count(){
        throw new CustomException("400", "错误，禁止请求访问");
    }

}
