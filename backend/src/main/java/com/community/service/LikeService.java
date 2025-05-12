package com.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.Like;

/**
 * 点赞服务接口
 */
public interface LikeService extends IService<Like> {

    /**
     * 点赞
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 类型：1-帖子，2-评论
     * @return 是否成功
     */
    boolean like(Long userId, Long targetId, Integer type);

    /**
     * 取消点赞
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 类型：1-帖子，2-评论
     * @return 是否成功
     */
    boolean unlike(Long userId, Long targetId, Integer type);

    /**
     * 是否已点赞
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 类型：1-帖子，2-评论
     * @return 是否已点赞
     */
    boolean isLiked(Long userId, Long targetId, Integer type);
}
