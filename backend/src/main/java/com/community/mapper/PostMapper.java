package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.entity.Post;
import com.community.vo.PostVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子Mapper接口
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 分页查询帖子列表
     * @param page 分页参数
     * @param categoryId 分类ID
     * @param ipId IP ID
     * @param keyword 关键词
     * @param userId 用户ID
     * @param status 状态
     * @param orderBy 排序字段
     * @param isAsc 是否升序
     * @return 帖子列表
     */
    Page<PostVO> selectPostPage(Page<PostVO> page,
                               @Param("categoryId") Long categoryId,
                               @Param("ipId") Long ipId,
                               @Param("keyword") String keyword,
                               @Param("userId") Long userId,
                               @Param("status") Integer status,
                               @Param("orderBy") String orderBy,
                               @Param("isAsc") Boolean isAsc);

    /**
     * 获取帖子详情
     * @param id 帖子ID
     * @return 帖子详情
     */
    PostVO selectPostDetail(@Param("id") Long id);

    /**
     * 根据ID列表查询帖子列表
     * @param page 分页参数
     * @param postIds 帖子ID列表
     * @return 帖子列表
     */
    Page<PostVO> selectPostsByIds(Page<PostVO> page, @Param("postIds") List<Long> postIds);
}
