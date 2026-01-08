package com.example.exception;

import com.example.common.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//ControllerAdvice定义全局异常处理类,controller中的异常处理器(所有controller中的异常都要放在这里处理)
@ControllerAdvice("com.example.controller")
public class GlobalExceptionHandler {

    //Exception是所有异常的父类,其中的class是具体的异常类,这里捕获所有异常，并返回统一的错误信息
    @ExceptionHandler(Exception.class)
    //通过ResponseBody注解将Result对象转换为HTTP响应体中的内容(即JSON格式)
    @ResponseBody
    public Result error(Exception e) {
        e.printStackTrace();
        return Result.error();
    }

    @ExceptionHandler(CustomException.class)
    //通过ResponseBody注解将Result对象转换为HTTP响应体中的内容(即JSON格式)
    @ResponseBody
    public Result error(CustomException e) {
        return Result.error(e.getCode(),e.getMsg());
    }
}
