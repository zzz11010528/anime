package com.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.PostImage;

import java.util.List;

/**
 * 帖子图片服务接口
 */
public interface PostImageService extends IService<PostImage> {

    /**
     * 根据帖子ID获取图片列表
     * @param postId 帖子ID
     * @return 图片列表
     */
    List<PostImage> getByPostId(Long postId);

    /**
     * 删除帖子图片
     * @param postId 帖子ID
     * @return 是否成功
     */
    boolean deleteByPostId(Long postId);
}
