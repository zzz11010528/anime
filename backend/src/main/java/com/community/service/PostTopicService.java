package com.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.PostTopic;

import java.util.List;

/**
 * 帖子话题关联服务接口
 */
public interface PostTopicService extends IService<PostTopic> {

    /**
     * 批量保存帖子话题关联
     * @param postId 帖子ID
     * @param topicIds 话题ID列表
     * @return 是否成功
     */
    boolean saveBatch(Long postId, List<Long> topicIds);

    /**
     * 删除帖子话题关联
     * @param postId 帖子ID
     * @return 是否成功
     */
    boolean deleteByPostId(Long postId);

    /**
     * 根据帖子ID获取话题ID列表
     * @param postId 帖子ID
     * @return 话题ID列表
     */
    List<Long> getTopicIdsByPostId(Long postId);
}
