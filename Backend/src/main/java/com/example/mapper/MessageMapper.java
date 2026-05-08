package com.example.mapper;

import com.example.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {
    void insert(Message message);

    // 查询某人的未读消息数量
    int countUnread(String account);

    // 查询某人的所有消息（按时间倒序）
    List<Message> selectByReceiver(String account);

    // 标记为已读
    void markAsRead(Integer id);
    // 【新增】标记某用户关于某商品的所有消息为已读（用于点击跳转后自动消除红点）
    void markRelatedAsRead(@Param("account") String account, @Param("relatedId") Integer relatedId);

    void deleteByReceiverAccount(String account);
}
