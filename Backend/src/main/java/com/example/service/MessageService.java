package com.example.service;

import com.example.entity.Message;
import com.example.mapper.MessageMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    @Resource
    private MessageMapper messageMapper;

    /**
     * 通用发送消息方法
     * @param receiver 接收人账号
     * @param title 标题
     * @param content 内容
     * @param type 类型 (0:系统, 1:竞价, 2:订单)
     * @param relatedId 关联ID
     */
    public void sendMessage(String receiver, String title, String content, int type, Integer relatedId) {
        Message msg = new Message();
        msg.setReceiverAccount(receiver);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setType(type);
        msg.setIsRead(0); // 默认为未读
        msg.setCreateTime(LocalDateTime.now());
        msg.setRelatedId(relatedId);
        messageMapper.insert(msg);
    }

    public List<Message> getMyMessages(String account) {
        return messageMapper.selectByReceiver(account);
    }

    public int getUnreadCount(String account) {
        return messageMapper.countUnread(account);
    }

    public void markAsRead(Integer id) {
        messageMapper.markAsRead(id);
    }
    
    // 【新增】点击消息跳转时调用，把关于这个商品的所有提醒都标为已读
    public void markRelatedAsRead(String account, Integer relatedId) {
        if (relatedId != null) {
            messageMapper.markRelatedAsRead(account, relatedId);
        }
    }
}
