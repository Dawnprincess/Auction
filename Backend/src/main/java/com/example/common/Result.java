//包装类,统一返回结果(可能是string,map,int等,在这里统一为Object)的数据类型
package com.example.common;


public class Result {
    private String code;
    //Object在java中可以表示任何类型的数据,包括基本数据类型,对象,数组等,是所有类型的父类
    private Object data;
    private String msg;

    public static Result success(){
        Result result = new Result();
        result.setCode("200");
        result.setMsg("请求成功");
        return result;
    }
    public static Result success(Object data){
        Result result = success();
        result.setData(data);
        return result;
    }
    public static Result error(){
        Result result = new Result();
        result.setCode("500");
        result.setMsg("系统异常");
        return result;
    }

    public static Result error(String code, String msg){
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
