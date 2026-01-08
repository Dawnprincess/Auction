package com.example.exception;

//继承RuntimeException类
public class CustomException extends RuntimeException{
    private String msg;
    private String code;

    public CustomException(String code, String msg) {
        this.msg = msg;
        this.code = code;
    }

    public String getMsg(){
        return msg;
    }
    public String getCode(){
        return code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
