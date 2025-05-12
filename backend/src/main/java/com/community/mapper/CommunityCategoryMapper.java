package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.CommunityCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社区分类Mapper接口
 */
@Mapper
public interface CommunityCategoryMapper extends BaseMapper<CommunityCategory> {
}
