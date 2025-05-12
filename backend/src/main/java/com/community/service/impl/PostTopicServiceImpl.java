package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.entity.PostTopic;
import com.community.mapper.PostTopicMapper;
import com.community.service.PostTopicService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 帖子话题关联服务实现类
 */
@Service
public class PostTopicServiceImpl extends ServiceImpl<PostTopicMapper, PostTopic> implements PostTopicService {

    @Override
    public boolean saveBatch(Long postId, List<Long> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            return true;
        }
        
        List<PostTopic> postTopics = new ArrayList<>();
        for (Long topicId : topicIds) {
            PostTopic postTopic = new PostTopic();
            postTopic.setPostId(postId);
            postTopic.setTopicId(topicId);
            postTopics.add(postTopic);
        }
        
        return saveBatch(postTopics);
    }

    @Override
    public boolean deleteByPostId(Long postId) {
        LambdaQueryWrapper<PostTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostTopic::getPostId, postId);
        return remove(wrapper);
    }

    @Override
    public List<Long> getTopicIdsByPostId(Long postId) {
        LambdaQueryWrapper<PostTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostTopic::getPostId, postId);
        List<PostTopic> postTopics = list(wrapper);
        
        return postTopics.stream()
                .map(PostTopic::getTopicId)
                .collect(Collectors.toList());
    }
}
