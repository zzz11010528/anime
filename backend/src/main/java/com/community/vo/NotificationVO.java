package com.community.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知VO
 */
@Data
public class NotificationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 类型：1-点赞，2-评论，3-回复，4-系统通知
     */
    private Integer type;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 通知内容
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
     * 操作用户ID
     */
    private Long operateUserId;

    /**
     * 操作用户名
     */
    private String operateUsername;

    /**
     * 操作用户昵称
     */
    private String operateNickname;

    /**
     * 操作用户头像
     */
    private String operateAvatar;

    /**
     * 目标标题（帖子标题或评论内容）
     */
    private String targetTitle;
}
