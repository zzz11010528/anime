package com.community.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.entity.Comment;
import com.community.entity.Post;
import com.community.entity.User;
import com.community.event.CommentEvent;
import com.community.mapper.CommentMapper;
import com.community.service.CommentService;
import com.community.service.PostService;
import com.community.service.UserService;
import com.community.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 评论服务实现类
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final UserService userService;
    private final PostService postService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(Comment comment) {
        // 校验用户是否存在
        User user = userService.getById(comment.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验帖子是否存在
        Post post = postService.getById(comment.getPostId());
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        // 校验帖子状态
        if (post.getStatus() != 1) {
            // 如果帖子状态不是正常，检查评论者是否为帖子作者或管理员
            boolean isAuthorOrAdmin = false;
            try {
                Long userId = comment.getUserId();
                // 检查是否为帖子作者
                isAuthorOrAdmin = userId.equals(post.getUserId());

                // 如果不是作者，检查是否为管理员
                if (!isAuthorOrAdmin) {
                    isAuthorOrAdmin = cn.dev33.satoken.stp.StpUtil.hasRole("admin");
                }
            } catch (Exception e) {
                // 异常情况，视为普通用户
            }

            // 如果既不是作者也不是管理员，则不允许评论
            if (!isAuthorOrAdmin) {
                throw new BusinessException("帖子状态异常");
            }
        }

        // 校验父评论
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            Comment parentComment = getById(comment.getParentId());
            if (parentComment == null) {
                throw new BusinessException("父评论不存在");
            }
            if (parentComment.getStatus() != 1) {
                throw new BusinessException("父评论状态异常");
            }
        } else {
            comment.setParentId(0L);
        }

        // 校验回复用户
        if (comment.getReplyUserId() != null && comment.getReplyUserId() > 0) {
            User replyUser = userService.getById(comment.getReplyUserId());
            if (replyUser == null) {
                throw new BusinessException("回复用户不存在");
            }
        }

        // 设置初始值
        comment.setLikeCount(0);
        comment.setStatus(1);

        // 保存评论
        save(comment);

        // 更新帖子评论数
        postService.updateCommentCount(comment.getPostId(), 1);

        // 发布评论事件
        eventPublisher.publishEvent(new CommentEvent(comment));

        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        // 校验评论是否存在
        Comment comment = getById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 删除评论（逻辑删除）
        comment.setStatus(0);
        updateById(comment);

        // 更新帖子评论数
        postService.updateCommentCount(comment.getPostId(), -1);

        return true;
    }

    @Override
    public boolean updateLikeCount(Long id, int count) {
        // 校验评论是否存在
        Comment comment = getById(id);
        if (comment == null) {
            return false;
        }

        // 更新点赞数
        comment.setLikeCount(comment.getLikeCount() + count);
        if (comment.getLikeCount() < 0) {
            comment.setLikeCount(0);
        }
        return updateById(comment);
    }

    @Override
    public Page<CommentVO> page(Long postId, Long parentId, Integer page, Integer size) {
        Page<CommentVO> pageParam = new Page<>(page, size);
        return baseMapper.selectCommentPage(pageParam, postId, parentId);
    }

    @Override
    public CommentVO getDetail(Long id) {
        // 校验评论是否存在
        Comment comment = getById(id);
        if (comment == null || comment.getStatus() != 1) {
            return null;
        }

        // 查询评论详情
        return baseMapper.selectCommentDetail(id);
    }
}
