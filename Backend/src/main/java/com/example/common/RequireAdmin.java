package com.example.common;

import java.lang.annotation.*;

//创建权限注解requireAdmin, 用于判断用户是否为管理员
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAdmin {
}

