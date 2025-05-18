package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.entity.MerchantCertification;
import com.community.entity.User;
import com.community.mapper.MerchantCertificationMapper;
import com.community.service.MerchantCertificationService;
import com.community.service.UserService;
import com.community.vo.MerchantCertificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商家认证服务实现类
 */
@Service
@RequiredArgsConstructor
public class MerchantCertificationServiceImpl extends ServiceImpl<MerchantCertificationMapper, MerchantCertification> implements MerchantCertificationService {

    private final UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean apply(MerchantCertification certification) {
        // 校验用户是否存在
        User user = userService.getById(certification.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验是否已申请
        MerchantCertification existCertification = getByUserId(certification.getUserId());
        if (existCertification != null) {
            if (existCertification.getCertificationStatus() == 0) {
                throw new BusinessException("您已提交认证申请，请等待审核");
            } else if (existCertification.getCertificationStatus() == 1) {
                throw new BusinessException("您已通过认证，无需重复申请");
            } else if (existCertification.getCertificationStatus() == 2) {
                // 如果之前的认证被拒绝，则更新现有记录而不是创建新记录
                // 复制新提交的认证信息到现有记录
                existCertification.setCompanyName(certification.getCompanyName());
                existCertification.setBusinessLicense(certification.getBusinessLicense());
                existCertification.setContactName(certification.getContactName());
                existCertification.setContactPhone(certification.getContactPhone());
                existCertification.setContactEmail(certification.getContactEmail());
                // 重置认证状态为审核中
                existCertification.setCertificationStatus(0);
                // 清空拒绝原因
                existCertification.setRejectReason(null);

                return updateById(existCertification);
            }
        }

        // 设置认证状态为审核中
        certification.setCertificationStatus(0);

        return save(certification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(Long id, Integer status, String rejectReason) {
        // 校验认证信息是否存在
        MerchantCertification certification = getById(id);
        if (certification == null) {
            throw new BusinessException("认证信息不存在");
        }

        // 校验认证状态
        if (certification.getCertificationStatus() != 0) {
            throw new BusinessException("该认证申请已处理");
        }

        // 更新认证状态
        certification.setCertificationStatus(status);
        if (status == 2) {
            certification.setRejectReason(rejectReason);
        }

        boolean result = updateById(certification);

        // 如果审核通过，更新用户角色为商家
        if (status == 1) {
            User user = userService.getById(certification.getUserId());
            user.setRole(1); // 设置为商家角色
            userService.updateById(user);
        }

        return result;
    }

    @Override
    public MerchantCertification getByUserId(Long userId) {
        LambdaQueryWrapper<MerchantCertification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantCertification::getUserId, userId);
        return getOne(wrapper);
    }

    @Override
    public Page<MerchantCertificationVO> getCertificationList(Integer page, Integer size, String companyName, String contactName, Integer status) {
        // 创建查询条件
        LambdaQueryWrapper<MerchantCertification> wrapper = new LambdaQueryWrapper<>();

        if (companyName != null && !companyName.isEmpty()) {
            wrapper.like(MerchantCertification::getCompanyName, companyName);
        }

        if (contactName != null && !contactName.isEmpty()) {
            wrapper.like(MerchantCertification::getContactName, contactName);
        }

        if (status != null) {
            wrapper.eq(MerchantCertification::getCertificationStatus, status);
        }

        // 分页查询认证信息
        Page<MerchantCertification> certPage = page(new Page<>(page, size), wrapper);

        // 获取所有用户ID
        List<Long> userIds = certPage.getRecords().stream()
                .map(MerchantCertification::getUserId)
                .collect(Collectors.toList());

        // 查询用户信息
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userService.listByIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
        }

        // 构建返回结果
        List<MerchantCertificationVO> voList = new ArrayList<>();
        for (MerchantCertification cert : certPage.getRecords()) {
            MerchantCertificationVO vo = new MerchantCertificationVO();
            BeanUtils.copyProperties(cert, vo);

            // 添加用户信息
            User user = userMap.get(cert.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
            }

            voList.add(vo);
        }

        // 构建分页结果
        Page<MerchantCertificationVO> result = new Page<>(certPage.getCurrent(), certPage.getSize(), certPage.getTotal());
        result.setRecords(voList);

        return result;
    }
}
