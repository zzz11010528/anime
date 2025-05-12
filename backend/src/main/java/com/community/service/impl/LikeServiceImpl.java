package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.entity.Like;
import com.community.event.LikeEvent;
import com.community.mapper.LikeMapper;
import com.community.service.CommentService;
import com.community.service.LikeService;
import com.community.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 点赞服务实现类
 */
@Service
@RequiredArgsConstructor
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeService {

    private final PostService postService;
    private final CommentService commentService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean like(Long userId, Long targetId, Integer type) {
        // 校验是否已点赞
        if (isLiked(userId, targetId, type)) {
            return true;
        }

        // 校验目标是否存在
        if (type == 1) {
            // 帖子
            if (postService.getById(targetId) == null) {
                throw new BusinessException("帖子不存在");
            }
        } else if (type == 2) {
            // 评论
            if (commentService.getById(targetId) == null) {
                throw new BusinessException("评论不存在");
            }
        } else {
            throw new BusinessException("不支持的点赞类型");
        }

        // 保存点赞记录
        Like like = new Like();
        like.setUserId(userId);
        like.setTargetId(targetId);
        like.setType(type);
        boolean result = save(like);

        // 更新点赞数
        if (result) {
            if (type == 1) {
                // 帖子
                postService.updateLikeCount(targetId, 1);
            } else if (type == 2) {
                // 评论
                commentService.updateLikeCount(targetId, 1);
            }

            // 发布点赞事件
            eventPublisher.publishEvent(new LikeEvent(like.getId(), userId, targetId, type));
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlike(Long userId, Long targetId, Integer type) {
        // 校验是否已点赞
        if (!isLiked(userId, targetId, type)) {
            return true;
        }

        // 删除点赞记录
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId);
        wrapper.eq(Like::getTargetId, targetId);
        wrapper.eq(Like::getType, type);
        boolean result = remove(wrapper);

        // 更新点赞数
        if (result) {
            if (type == 1) {
                // 帖子
                postService.updateLikeCount(targetId, -1);
            } else if (type == 2) {
                // 评论
                commentService.updateLikeCount(targetId, -1);
            }
        }

        return result;
    }

    @Override
    public boolean isLiked(Long userId, Long targetId, Integer type) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId);
        wrapper.eq(Like::getTargetId, targetId);
        wrapper.eq(Like::getType, type);
        return count(wrapper) > 0;
    }
}
