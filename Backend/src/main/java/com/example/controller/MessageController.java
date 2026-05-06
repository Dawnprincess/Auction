package com.example.controller;

import cn.hutool.json.JSONUtil;
import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.Message;
import com.example.service.MessageService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    /**
     * 获取当前登录用户的消息列表
     */
    @GetMapping("/list")
    public Result list(HttpServletRequest request) {
        String userHeader = request.getHeader("X-User-Info");
        if (userHeader == null) return Result.error("401", "请先登录");

        Account accountObj = JSONUtil.toBean(userHeader, Account.class);
        List<Message> list = messageService.getMyMessages(accountObj.getAccount());
        return Result.success(list);
    }

    /**
     * 获取未读消息数量（用于右上角红点）
     */
    @GetMapping("/unread/count")
    public Result unreadCount(HttpServletRequest request) {
        String userHeader = request.getHeader("X-User-Info");
        if (userHeader == null) return Result.success(0);

        Account accountObj = JSONUtil.toBean(userHeader, Account.class);
        int count = messageService.getUnreadCount(accountObj.getAccount());
        return Result.success(count);
    }

    /**
     * 标记单条消息为已读
     */
    @PostMapping("/read/{id}")
    public Result read(@PathVariable Integer id) {
        messageService.markAsRead(id);
        return Result.success();
    }

    /**
     * 【关键】点击消息跳转时，标记相关业务消息为已读
     * 例如：点击了关于商品21的消息，则把所有关于商品21的未读消息全部标红
     */
    @PostMapping("/read/related/{relatedId}")
    public Result readRelated(@PathVariable Integer relatedId, HttpServletRequest request) {
        String userHeader = request.getHeader("X-User-Info");
        if (userHeader == null) return Result.error("401", "请先登录");

        Account accountObj = JSONUtil.toBean(userHeader, Account.class);
        messageService.markRelatedAsRead(accountObj.getAccount(), relatedId);
        return Result.success();
    }
}
