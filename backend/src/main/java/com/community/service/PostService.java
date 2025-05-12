package com.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.Post;
import com.community.entity.PostImage;
import com.community.vo.PostVO;

import java.util.List;

/**
 * 帖子服务接口
 */
public interface PostService extends IService<Post> {

    /**
     * 发布帖子
     * @param post 帖子信息
     * @param images 帖子图片
     * @param topicIds 话题ID列表
     * @return 帖子ID
     */
    Long publish(Post post, List<PostImage> images, List<Long> topicIds);

    /**
     * 更新帖子
     * @param post 帖子信息
     * @param images 帖子图片
     * @param topicIds 话题ID列表
     * @return 是否成功
     */
    boolean update(Post post, List<PostImage> images, List<Long> topicIds);

    /**
     * 删除帖子
     * @param id 帖子ID
     * @return 是否成功
     */
    boolean delete(Long id);

    /**
     * 审核帖子
     * @param id 帖子ID
     * @param status 审核状态：1-通过，0-拒绝
     * @param rejectReason 拒绝原因（拒绝时必填）
     * @return 是否成功
     */
    boolean audit(Long id, Integer status, String rejectReason);

    /**
     * 增加浏览量
     * @param id 帖子ID
     * @return 是否成功
     */
    boolean increaseViewCount(Long id);

    /**
     * 更新点赞数
     * @param id 帖子ID
     * @param count 增加的数量，可以为负数
     * @return 是否成功
     */
    boolean updateLikeCount(Long id, int count);

    /**
     * 更新评论数
     * @param id 帖子ID
     * @param count 增加的数量，可以为负数
     * @return 是否成功
     */
    boolean updateCommentCount(Long id, int count);

    /**
     * 更新收藏数
     * @param id 帖子ID
     * @param count 增加的数量，可以为负数
     * @return 是否成功
     */
    boolean updateCollectCount(Long id, int count);

    /**
     * 分页查询帖子列表
     * @param page 页码
     * @param size 每页大小
     * @param categoryId 分类ID
     * @param ipId IP ID
     * @param keyword 关键词
     * @param userId 用户ID
     * @param status 状态
     * @param orderBy 排序字段
     * @param isAsc 是否升序
     * @return 帖子列表
     */
    Page<PostVO> page(Integer page, Integer size, Long categoryId, Long ipId, String keyword, Long userId, Integer status, String orderBy, Boolean isAsc);

    /**
     * 获取帖子详情
     * @param id 帖子ID
     * @return 帖子详情
     */
    PostVO getDetail(Long id);

    /**
     * 获取用户发布的帖子列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 帖子列表
     */
    Page<PostVO> getUserPosts(Long userId, Integer page, Integer size);
}
