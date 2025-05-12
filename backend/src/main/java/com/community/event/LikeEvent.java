package com.community.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点赞事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeEvent {

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
}
