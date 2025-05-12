package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.entity.GroupBuying;
import com.community.entity.Product;
import com.community.mapper.GroupBuyingMapper;
import com.community.service.GroupBuyingService;
import com.community.service.ProductService;
import com.community.vo.GroupBuyingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 拼团活动服务实现类
 */
@Service
@RequiredArgsConstructor
public class GroupBuyingServiceImpl extends ServiceImpl<GroupBuyingMapper, GroupBuying> implements GroupBuyingService {

    private final ProductService productService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(GroupBuying groupBuying) {
        // 校验商品是否存在
        Product product = productService.getById(groupBuying.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 校验商品是否上架
        if (product.getStatus() != 1) {
            throw new BusinessException("商品未上架，不能创建拼团活动");
        }

        // 校验拼团价格
        if (groupBuying.getGroupPrice().compareTo(product.getPrice()) >= 0) {
            throw new BusinessException("拼团价格必须低于商品原价");
        }

        // 校验成团人数
        if (groupBuying.getMinGroupSize() < 2) {
            throw new BusinessException("最小成团人数不能小于2");
        }
        if (groupBuying.getMaxGroupSize() < groupBuying.getMinGroupSize()) {
            throw new BusinessException("最大成团人数不能小于最小成团人数");
        }

        // 校验活动时间
        LocalDateTime now = LocalDateTime.now();
        if (groupBuying.getStartTime().isBefore(now)) {
            throw new BusinessException("开始时间不能早于当前时间");
        }
        if (groupBuying.getEndTime().isBefore(groupBuying.getStartTime())) {
            throw new BusinessException("结束时间不能早于开始时间");
        }

        // 设置状态
        if (groupBuying.getStartTime().isAfter(now)) {
            groupBuying.setStatus(2); // 未开始
        } else if (groupBuying.getEndTime().isAfter(now)) {
            groupBuying.setStatus(1); // 进行中
        } else {
            groupBuying.setStatus(0); // 已结束
        }

        // 保存拼团活动
        boolean result = save(groupBuying);
        if (!result) {
            throw new BusinessException("创建拼团活动失败");
        }

        return groupBuying.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(GroupBuying groupBuying) {
        // 校验拼团活动是否存在
        GroupBuying existGroupBuying = getById(groupBuying.getId());
        if (existGroupBuying == null) {
            throw new BusinessException("拼团活动不存在");
        }

        // 校验状态，只有未开始的拼团活动才能修改
        if (existGroupBuying.getStatus() != 2) {
            throw new BusinessException("只有未开始的拼团活动才能修改");
        }

        // 校验商品是否存在
        Product product = productService.getById(groupBuying.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 校验商品是否上架
        if (product.getStatus() != 1) {
            throw new BusinessException("商品未上架，不能创建拼团活动");
        }

        // 校验拼团价格
        if (groupBuying.getGroupPrice().compareTo(product.getPrice()) >= 0) {
            throw new BusinessException("拼团价格必须低于商品原价");
        }

        // 校验成团人数
        if (groupBuying.getMinGroupSize() < 2) {
            throw new BusinessException("最小成团人数不能小于2");
        }
        if (groupBuying.getMaxGroupSize() < groupBuying.getMinGroupSize()) {
            throw new BusinessException("最大成团人数不能小于最小成团人数");
        }

        // 校验活动时间
        LocalDateTime now = LocalDateTime.now();
        if (groupBuying.getStartTime().isBefore(now)) {
            throw new BusinessException("开始时间不能早于当前时间");
        }
        if (groupBuying.getEndTime().isBefore(groupBuying.getStartTime())) {
            throw new BusinessException("结束时间不能早于开始时间");
        }

        // 设置状态
        if (groupBuying.getStartTime().isAfter(now)) {
            groupBuying.setStatus(2); // 未开始
        } else if (groupBuying.getEndTime().isAfter(now)) {
            groupBuying.setStatus(1); // 进行中
        } else {
            groupBuying.setStatus(0); // 已结束
        }

        // 更新拼团活动
        return updateById(groupBuying);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean end(Long id) {
        // 校验拼团活动是否存在
        GroupBuying groupBuying = getById(id);
        if (groupBuying == null) {
            throw new BusinessException("拼团活动不存在");
        }

        // 校验状态，只有进行中或未开始的拼团活动才能结束
        if (groupBuying.getStatus() == 0) {
            throw new BusinessException("拼团活动已结束");
        }

        // 设置状态为已结束
        groupBuying.setStatus(0);
        groupBuying.setEndTime(LocalDateTime.now());

        // 更新拼团活动
        return updateById(groupBuying);
    }

    @Override
    public Page<GroupBuyingVO> page(Long sellerId, String keyword, Integer status, Integer page, Integer size) {
        Page<GroupBuyingVO> pageParam = new Page<>(page, size);
        return baseMapper.selectGroupBuyingPage(pageParam, sellerId, keyword, status);
    }

    @Override
    public GroupBuyingVO getDetail(Long id) {
        return baseMapper.selectGroupBuyingDetail(id);
    }

    @Override
    public List<GroupBuyingVO> getActiveGroupBuyings(Integer limit) {
        // 查询正在进行中的拼团活动
        LambdaQueryWrapper<GroupBuying> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuying::getStatus, 1); // 进行中的拼团
        wrapper.orderByDesc(GroupBuying::getCreatedAt);
        wrapper.last("LIMIT " + limit);

        List<GroupBuying> groupBuyings = list(wrapper);
        if (groupBuyings.isEmpty()) {
            return Collections.emptyList();
        }

        // 转换为VO
        List<GroupBuyingVO> result = new ArrayList<>();
        for (GroupBuying groupBuying : groupBuyings) {
            GroupBuyingVO vo = baseMapper.selectGroupBuyingDetail(groupBuying.getId());
            if (vo != null) {
                result.add(vo);
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkAndUpdateStatus() {
        int count = 0;
        LocalDateTime now = LocalDateTime.now();

        // 查询未开始的拼团活动，检查是否到了开始时间
        LambdaQueryWrapper<GroupBuying> startWrapper = new LambdaQueryWrapper<>();
        startWrapper.eq(GroupBuying::getStatus, 2)
                .le(GroupBuying::getStartTime, now);
        List<GroupBuying> startList = list(startWrapper);
        for (GroupBuying groupBuying : startList) {
            groupBuying.setStatus(1);
            updateById(groupBuying);
            count++;
        }

        // 查询进行中的拼团活动，检查是否到了结束时间
        LambdaQueryWrapper<GroupBuying> endWrapper = new LambdaQueryWrapper<>();
        endWrapper.eq(GroupBuying::getStatus, 1)
                .le(GroupBuying::getEndTime, now);
        List<GroupBuying> endList = list(endWrapper);
        for (GroupBuying groupBuying : endList) {
            groupBuying.setStatus(0);
            updateById(groupBuying);
            count++;
        }

        return count;
    }
}
