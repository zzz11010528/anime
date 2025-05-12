package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.entity.PostImage;
import com.community.mapper.PostImageMapper;
import com.community.service.PostImageService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 帖子图片服务实现类
 */
@Service
public class PostImageServiceImpl extends ServiceImpl<PostImageMapper, PostImage> implements PostImageService {

    @Override
    public List<PostImage> getByPostId(Long postId) {
        LambdaQueryWrapper<PostImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostImage::getPostId, postId);
        wrapper.orderByAsc(PostImage::getSort);
        return list(wrapper);
    }

    @Override
    public boolean deleteByPostId(Long postId) {
        LambdaQueryWrapper<PostImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostImage::getPostId, postId);
        return remove(wrapper);
    }
}
