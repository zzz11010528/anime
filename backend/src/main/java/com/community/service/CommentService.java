package com.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.Comment;
import com.community.vo.CommentVO;

/**
 * 评论服务接口
 */
public interface CommentService extends IService<Comment> {

    /**
     * 发表评论
     * @param comment 评论信息
     * @return 评论ID
     */
    Long publish(Comment comment);

    /**
     * 删除评论
     * @param id 评论ID
     * @return 是否成功
     */
    boolean delete(Long id);

    /**
     * 更新点赞数
     * @param id 评论ID
     * @param count 增加的数量，可以为负数
     * @return 是否成功
     */
    boolean updateLikeCount(Long id, int count);

    /**
     * 分页查询评论列表
     * @param postId 帖子ID
     * @param parentId 父评论ID，0表示一级评论
     * @param page 页码
     * @param size 每页大小
     * @return 评论列表
     */
    Page<CommentVO> page(Long postId, Long parentId, Integer page, Integer size);

    /**
     * 获取评论详情
     * @param id 评论ID
     * @return 评论详情
     */
    CommentVO getDetail(Long id);
}
