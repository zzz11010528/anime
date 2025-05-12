package com.community.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信VO
 */
@Data
public class MessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 发送者ID
     */
    private Long fromUserId;

    /**
     * 发送者用户名
     */
    private String fromUsername;

    /**
     * 发送者昵称
     */
    private String fromNickname;

    /**
     * 发送者头像
     */
    private String fromAvatar;

    /**
     * 接收者ID
     */
    private Long toUserId;

    /**
     * 接收者用户名
     */
    private String toUsername;

    /**
     * 接收者昵称
     */
    private String toNickname;

    /**
     * 接收者头像
     */
    private String toAvatar;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 未读消息数量（用于会话列表）
     */
    private Integer unreadCount;

    /**
     * 最后一条消息内容（用于会话列表）
     */
    private String lastMessage;

    /**
     * 最后一条消息时间（用于会话列表）
     */
    private LocalDateTime lastMessageTime;
}
