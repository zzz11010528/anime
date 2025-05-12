package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.PostImage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子图片Mapper接口
 */
@Mapper
public interface PostImageMapper extends BaseMapper<PostImage> {
}
