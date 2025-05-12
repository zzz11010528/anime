package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.entity.UserCollection;
import com.community.mapper.CollectionMapper;
import com.community.mapper.PostMapper;
import com.community.mapper.ProductMapper;
import com.community.service.CollectionService;
import com.community.service.PostService;
import com.community.service.ProductService;
import com.community.vo.PostVO;
import com.community.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏服务实现类
 */
@Service
@RequiredArgsConstructor
public class CollectionServiceImpl extends ServiceImpl<CollectionMapper, UserCollection> implements CollectionService {

    private final PostService postService;
    private final ProductService productService;
    private final PostMapper postMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean collect(Long userId, Long targetId, Integer type) {
        // 校验是否已收藏
        if (isCollected(userId, targetId, type)) {
            return true;
        }

        // 校验目标是否存在
        if (type == 1) {
            // 帖子
            if (postService.getById(targetId) == null) {
                throw new BusinessException("帖子不存在");
            }
        } else if (type == 2) {
            // 商品
            if (productService.getById(targetId) == null) {
                throw new BusinessException("商品不存在");
            }
        } else {
            throw new BusinessException("不支持的收藏类型");
        }

        // 保存收藏记录
        UserCollection collection = new UserCollection();
        collection.setUserId(userId);
        collection.setTargetId(targetId);
        collection.setType(type);
        boolean result = save(collection);

        // 更新收藏数
        if (result) {
            if (type == 1) {
                // 帖子
                postService.updateCollectCount(targetId, 1);
            } else if (type == 2) {
                // 商品
                productService.updateCollectCount(targetId, 1);
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean uncollect(Long userId, Long targetId, Integer type) {
        // 校验是否已收藏
        if (!isCollected(userId, targetId, type)) {
            return true;
        }

        // 删除收藏记录
        LambdaQueryWrapper<UserCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollection::getUserId, userId);
        wrapper.eq(UserCollection::getTargetId, targetId);
        wrapper.eq(UserCollection::getType, type);
        boolean result = remove(wrapper);

        // 更新收藏数
        if (result) {
            if (type == 1) {
                // 帖子
                postService.updateCollectCount(targetId, -1);
            } else if (type == 2) {
                // 商品
                productService.updateCollectCount(targetId, -1);
            }
        }

        return result;
    }

    @Override
    public boolean isCollected(Long userId, Long targetId, Integer type) {
        LambdaQueryWrapper<UserCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollection::getUserId, userId);
        wrapper.eq(UserCollection::getTargetId, targetId);
        wrapper.eq(UserCollection::getType, type);
        return count(wrapper) > 0;
    }

    @Override
    public Page<PostVO> getCollectedPosts(Long userId, Integer page, Integer size) {
        // 获取用户收藏的帖子ID列表
        LambdaQueryWrapper<UserCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollection::getUserId, userId);
        wrapper.eq(UserCollection::getType, 1);
        List<UserCollection> collections = list(wrapper);

        if (collections.isEmpty()) {
            return new Page<>(page, size);
        }

        List<Long> postIds = collections.stream()
                .map(UserCollection::getTargetId)
                .collect(Collectors.toList());

        // 根据帖子ID列表查询帖子列表
        Page<PostVO> pageParam = new Page<>(page, size);
        return postMapper.selectPostsByIds(pageParam, postIds);
    }

    @Override
    public Page<ProductVO> getCollectedProducts(Long userId, Integer page, Integer size) {
        // 获取用户收藏的商品ID列表
        LambdaQueryWrapper<UserCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollection::getUserId, userId);
        wrapper.eq(UserCollection::getType, 2);
        List<UserCollection> collections = list(wrapper);

        if (collections.isEmpty()) {
            return new Page<>(page, size);
        }

        List<Long> productIds = collections.stream()
                .map(UserCollection::getTargetId)
                .collect(Collectors.toList());

        // 根据商品ID列表查询商品列表
        Page<ProductVO> pageParam = new Page<>(page, size);
        return productMapper.selectProductsByIds(pageParam, productIds);
    }
}
