package com.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.UserCollection;
import com.community.vo.PostVO;
import com.community.vo.ProductVO;

/**
 * 收藏服务接口
 */
public interface CollectionService extends IService<UserCollection> {

    /**
     * 收藏
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 类型：1-帖子，2-商品
     * @return 是否成功
     */
    boolean collect(Long userId, Long targetId, Integer type);

    /**
     * 取消收藏
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 类型：1-帖子，2-商品
     * @return 是否成功
     */
    boolean uncollect(Long userId, Long targetId, Integer type);

    /**
     * 是否已收藏
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 类型：1-帖子，2-商品
     * @return 是否已收藏
     */
    boolean isCollected(Long userId, Long targetId, Integer type);

    /**
     * 获取用户收藏的帖子列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 帖子列表
     */
    Page<PostVO> getCollectedPosts(Long userId, Integer page, Integer size);

    /**
     * 获取用户收藏的商品列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 商品列表
     */
    Page<ProductVO> getCollectedProducts(Long userId, Integer page, Integer size);
}
