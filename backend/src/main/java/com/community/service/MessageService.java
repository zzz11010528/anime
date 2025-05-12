package com.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.Message;
import com.community.vo.MessageVO;

import java.util.List;

/**
 * 私信服务接口
 */
public interface MessageService extends IService<Message> {

    /**
     * 发送私信
     * @param fromUserId 发送者ID
     * @param toUserId 接收者ID
     * @param content 消息内容
     * @return 消息ID
     */
    Long send(Long fromUserId, Long toUserId, String content);

    /**
     * 标记为已读
     * @param id 消息ID
     * @return 是否成功
     */
    boolean markAsRead(Long id);

    /**
     * 标记会话为已读
     * @param userId 用户ID
     * @param targetUserId 目标用户ID
     * @return 是否成功
     */
    boolean markConversationAsRead(Long userId, Long targetUserId);

    /**
     * 分页查询私信列表
     * @param userId 用户ID
     * @param targetUserId 目标用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 私信列表
     */
    Page<MessageVO> page(Long userId, Long targetUserId, Integer page, Integer size);

    /**
     * 获取用户的私信会话列表
     * @param userId 用户ID
     * @return 私信会话列表
     */
    List<MessageVO> getConversationList(Long userId);

    /**
     * 获取未读私信数量
     * @param userId 用户ID
     * @return 未读私信数量
     */
    int getUnreadCount(Long userId);

    /**
     * 删除私信会话
     * @param userId 用户ID
     * @param targetUserId 目标用户ID
     * @return 是否成功
     */
    boolean deleteConversation(Long userId, Long targetUserId);
}
