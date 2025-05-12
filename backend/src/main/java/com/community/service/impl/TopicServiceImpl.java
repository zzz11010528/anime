package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.entity.PostTopic;
import com.community.entity.Topic;
import com.community.mapper.PostTopicMapper;
import com.community.mapper.TopicMapper;
import com.community.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 话题服务实现类
 */
@Service
@RequiredArgsConstructor
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    private final PostTopicMapper postTopicMapper;

    @Override
    public List<Topic> getByPostId(Long postId) {
        // 查询帖子话题关联
        LambdaQueryWrapper<PostTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostTopic::getPostId, postId);
        List<PostTopic> postTopics = postTopicMapper.selectList(wrapper);
        
        if (postTopics.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取话题ID列表
        List<Long> topicIds = postTopics.stream()
                .map(PostTopic::getTopicId)
                .collect(Collectors.toList());
        
        // 查询话题
        return listByIds(topicIds);
    }

    @Override
    public boolean updatePostCount(Long topicId, int count) {
        Topic topic = getById(topicId);
        if (topic == null) {
            return false;
        }
        
        // 更新帖子数量
        topic.setPostCount(topic.getPostCount() + count);
        if (topic.getPostCount() < 0) {
            topic.setPostCount(0);
        }
        
        return updateById(topic);
    }
}
