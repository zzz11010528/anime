package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.PostTopic;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子话题关联Mapper接口
 */
@Mapper
public interface PostTopicMapper extends BaseMapper<PostTopic> {
}
