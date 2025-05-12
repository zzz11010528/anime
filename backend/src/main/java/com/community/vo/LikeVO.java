package com.community.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 点赞VO
 */
@Data
public class LikeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 点赞ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 类型：1-帖子，2-评论
     */
    private Integer type;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 目标对象（帖子或评论）
     */
    private Object target;
}
