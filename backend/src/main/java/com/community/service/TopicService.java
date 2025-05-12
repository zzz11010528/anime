package com.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.Topic;

import java.util.List;

/**
 * 话题服务接口
 */
public interface TopicService extends IService<Topic> {

    /**
     * 根据帖子ID获取话题列表
     * @param postId 帖子ID
     * @return 话题列表
     */
    List<Topic> getByPostId(Long postId);

    /**
     * 更新帖子数量
     * @param topicId 话题ID
     * @param count 增加的数量，可以为负数
     * @return 是否成功
     */
    boolean updatePostCount(Long topicId, int count);
}
