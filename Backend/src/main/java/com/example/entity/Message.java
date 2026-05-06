package com.example.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Message {
    private Integer id;
    private String title;       // 消息标题
    private String content;     // 消息内容
    private String receiverAccount; // 接收人账号 (可以是用户 1xx 或管理员 0xx)
    private Integer type;       // 0-系统通知, 1-竞价提醒, 2-订单提醒
    private Integer isRead;     // 0-未读, 1-已读
    private LocalDateTime createTime;
    private Integer relatedId;  // 关联的商品ID或订单ID
}
