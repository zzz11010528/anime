package com.community.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.entity.Post;
import com.community.entity.PostImage;
import com.community.entity.User;
import com.community.mapper.PostMapper;
import com.community.service.*;
import com.community.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 帖子服务实现类
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final UserService userService;
    private final PostImageService postImageService;
    private final PostTopicService postTopicService;
    private final TopicService topicService;
    private final CommunityCategoryService communityCategoryService;
    private final IpService ipService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(Post post, List<PostImage> images, List<Long> topicIds) {
        // 校验用户是否存在
        User user = userService.getById(post.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验分类是否存在
        if (post.getCategoryId() != null && post.getCategoryId() > 0) {
            if (communityCategoryService.getById(post.getCategoryId()) == null) {
                throw new BusinessException("分类不存在");
            }
        }

        // 校验IP是否存在
        if (post.getIpId() != null && post.getIpId() > 0) {
            if (ipService.getById(post.getIpId()) == null) {
                throw new BusinessException("IP不存在");
            }
        }

        // 设置初始值
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCollectCount(0);

        // 设置帖子状态
        if (user.getRole() == 2) {
            // 管理员直接通过
            post.setStatus(1);
        } else {
            // 普通用户需要审核
            post.setStatus(2);
        }

        // 保存帖子
        save(post);

        // 保存帖子图片
        if (images != null && !images.isEmpty()) {
            for (PostImage image : images) {
                image.setPostId(post.getId());
            }
            postImageService.saveBatch(images);
        }

        // 保存帖子话题关联
        if (topicIds != null && !topicIds.isEmpty()) {
            postTopicService.saveBatch(post.getId(), topicIds);

            // 更新话题帖子数量
            for (Long topicId : topicIds) {
                topicService.updatePostCount(topicId, 1);
            }
        }

        return post.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Post post, List<PostImage> images, List<Long> topicIds) {
        // 校验帖子是否存在
        Post existPost = getById(post.getId());
        if (existPost == null) {
            throw new BusinessException("帖子不存在");
        }

        // 校验是否有权限修改
        if (!existPost.getUserId().equals(post.getUserId())) {
            throw new BusinessException("无权修改该帖子");
        }

        // 校验分类是否存在
        if (post.getCategoryId() != null && post.getCategoryId() > 0) {
            if (communityCategoryService.getById(post.getCategoryId()) == null) {
                throw new BusinessException("分类不存在");
            }
        }

        // 校验IP是否存在
        if (post.getIpId() != null && post.getIpId() > 0) {
            if (ipService.getById(post.getIpId()) == null) {
                throw new BusinessException("IP不存在");
            }
        }

        // 保留原有的计数字段
        post.setViewCount(existPost.getViewCount());
        post.setLikeCount(existPost.getLikeCount());
        post.setCommentCount(existPost.getCommentCount());
        post.setCollectCount(existPost.getCollectCount());

        // 设置帖子状态
        User user = userService.getById(post.getUserId());
        if (user.getRole() == 2) {
            // 管理员直接通过
            post.setStatus(1);
        } else {
            // 普通用户需要审核
            post.setStatus(2);
        }

        // 更新帖子
        updateById(post);

        // 删除原有帖子图片
        postImageService.deleteByPostId(post.getId());

        // 保存新的帖子图片
        if (images != null && !images.isEmpty()) {
            for (PostImage image : images) {
                image.setPostId(post.getId());
            }
            postImageService.saveBatch(images);
        }

        // 获取原有话题ID列表
        List<Long> oldTopicIds = postTopicService.getTopicIdsByPostId(post.getId());

        // 删除原有帖子话题关联
        postTopicService.deleteByPostId(post.getId());

        // 保存新的帖子话题关联
        if (topicIds != null && !topicIds.isEmpty()) {
            postTopicService.saveBatch(post.getId(), topicIds);
        }

        // 更新话题帖子数量
        if (oldTopicIds != null && !oldTopicIds.isEmpty()) {
            for (Long topicId : oldTopicIds) {
                topicService.updatePostCount(topicId, -1);
            }
        }
        if (topicIds != null && !topicIds.isEmpty()) {
            for (Long topicId : topicIds) {
                topicService.updatePostCount(topicId, 1);
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        // 校验帖子是否存在
        Post post = getById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        // 删除帖子
        removeById(id);

        // 删除帖子图片
        postImageService.deleteByPostId(id);

        // 获取话题ID列表
        List<Long> topicIds = postTopicService.getTopicIdsByPostId(id);

        // 删除帖子话题关联
        postTopicService.deleteByPostId(id);

        // 更新话题帖子数量
        if (topicIds != null && !topicIds.isEmpty()) {
            for (Long topicId : topicIds) {
                topicService.updatePostCount(topicId, -1);
            }
        }

        return true;
    }

    @Override
    public boolean audit(Long id, Integer status, String rejectReason) {
        // 校验帖子是否存在
        Post post = getById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        // 校验帖子状态
        if (post.getStatus() != 2) {
            throw new BusinessException("该帖子不在审核中");
        }

        // 如果是拒绝状态且没有拒绝原因，返回错误
        if (status == 0 && (rejectReason == null || rejectReason.isEmpty())) {
            throw new BusinessException("拒绝原因不能为空");
        }

        // 更新帖子状态
        post.setStatus(status);
        // TODO: 保存拒绝原因到数据库（如果需要）

        return updateById(post);
    }

    @Override
    public boolean increaseViewCount(Long id) {
        // 校验帖子是否存在
        Post post = getById(id);
        if (post == null) {
            return false;
        }

        // 增加浏览量
        post.setViewCount(post.getViewCount() + 1);
        return updateById(post);
    }

    @Override
    public boolean updateLikeCount(Long id, int count) {
        // 校验帖子是否存在
        Post post = getById(id);
        if (post == null) {
            return false;
        }

        // 更新点赞数
        post.setLikeCount(post.getLikeCount() + count);
        if (post.getLikeCount() < 0) {
            post.setLikeCount(0);
        }
        return updateById(post);
    }

    @Override
    public boolean updateCommentCount(Long id, int count) {
        // 校验帖子是否存在
        Post post = getById(id);
        if (post == null) {
            return false;
        }

        // 更新评论数
        post.setCommentCount(post.getCommentCount() + count);
        if (post.getCommentCount() < 0) {
            post.setCommentCount(0);
        }
        return updateById(post);
    }

    @Override
    public boolean updateCollectCount(Long id, int count) {
        // 校验帖子是否存在
        Post post = getById(id);
        if (post == null) {
            return false;
        }

        // 更新收藏数
        post.setCollectCount(post.getCollectCount() + count);
        if (post.getCollectCount() < 0) {
            post.setCollectCount(0);
        }
        return updateById(post);
    }

    @Override
    public Page<PostVO> page(Integer page, Integer size, Long categoryId, Long ipId, String keyword, Long userId, Integer status, String orderBy, Boolean isAsc) {
        Page<PostVO> pageParam = new Page<>(page, size);
        return baseMapper.selectPostPage(pageParam, categoryId, ipId, keyword, userId, status, orderBy, isAsc);
    }

    @Override
    public PostVO getDetail(Long id) {
        // 增加浏览量
        increaseViewCount(id);

        // 获取帖子详情
        PostVO postVO = baseMapper.selectPostDetail(id);

        if (postVO != null) {
            // 获取帖子图片
            List<PostImage> images = postImageService.getByPostId(id);
            postVO.setImages(images);

            // 获取帖子话题
            List<com.community.entity.Topic> topics = topicService.getByPostId(id);
            postVO.setTopics(topics);
        }

        return postVO;
    }

    @Override
    public Page<PostVO> getUserPosts(Long userId, Integer page, Integer size) {
        // 不传递status参数，查询用户的所有帖子（包括审核中的帖子）
        return this.page(page, size, null, null, null, userId, null, "created_at", false);
    }
}
