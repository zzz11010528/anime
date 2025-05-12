package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.entity.Comment;
import com.community.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 评论Mapper接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 分页查询评论列表
     * @param page 分页参数
     * @param postId 帖子ID
     * @param parentId 父评论ID
     * @return 评论列表
     */
    Page<CommentVO> selectCommentPage(Page<CommentVO> page,
                                     @Param("postId") Long postId,
                                     @Param("parentId") Long parentId);

    /**
     * 获取评论详情
     * @param id 评论ID
     * @return 评论详情
     */
    CommentVO selectCommentDetail(@Param("id") Long id);
}
