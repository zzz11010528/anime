package com.community.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.entity.CommunityCategory;
import com.community.mapper.CommunityCategoryMapper;
import com.community.service.CommunityCategoryService;
import org.springframework.stereotype.Service;

/**
 * 社区分类服务实现类
 */
@Service
public class CommunityCategoryServiceImpl extends ServiceImpl<CommunityCategoryMapper, CommunityCategory> implements CommunityCategoryService {
}
