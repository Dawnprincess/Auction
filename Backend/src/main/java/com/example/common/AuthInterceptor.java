package com.example.common;

import cn.hutool.json.JSONUtil;
import com.example.entity.Account;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    //preHandle在处理请求之前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.只处理方法级别的请求(忽略静态资源)
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        //2.判断当前方法或类上是否有RequireAdmin注解
        boolean requireAdmin = handlerMethod.hasMethodAnnotation(RequireAdmin.class) 
                || handlerMethod.getBeanType().isAnnotationPresent(RequireAdmin.class);

        //3.如果没有RequireAdmin注解则直接通过
        if (!requireAdmin) {
            return true;
        }

        //下面的代码只有在要求管理员权限的请求才执行检查
        //4.获取请求头中的用户信息,前端每次请求会在header中带上X-User-Info
        String userHeader = request.getHeader("X-User-Info");

        //5.如果用户信息为空,说明没登录,拦截禁止访问,则返回401错误
        if (userHeader == null || userHeader.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSONUtil.toJsonStr(Result.error("401", "未登录")));
            return false;
        }

        try {
            //6.转换JSON字符串为用户对象
            Account user = JSONUtil.toBean(userHeader, Account.class);

            //7.检查是否为管理员
            if (user.getAccessId() == null || user.getAccessId() != 0) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(JSONUtil.toJsonStr(Result.error("403", "权限不足，需要管理员权限")));
                return false;
            }
        } catch (Exception e) {
            //如果解析用户信息出错，拦截请求，返回401错误
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSONUtil.toJsonStr(Result.error("401", "用户信息解析失败")));
            return false;
        }

        //所有检测通过，放行
        return true;
    }
}
