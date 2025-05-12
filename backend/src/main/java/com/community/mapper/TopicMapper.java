package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.Topic;
import org.apache.ibatis.annotations.Mapper;

/**
 * 话题Mapper接口
 */
@Mapper
public interface TopicMapper extends BaseMapper<Topic> {
}
