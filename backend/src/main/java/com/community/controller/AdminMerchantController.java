package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.result.R;
import com.community.entity.MerchantCertification;
import com.community.entity.User;
import com.community.service.MerchantCertificationService;
import com.community.service.UserService;
import com.community.vo.MerchantVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员-商家管理控制器
 */
@RestController
@RequestMapping("/admin/merchant")
@RequiredArgsConstructor
public class AdminMerchantController {

    private final UserService userService;
    private final MerchantCertificationService merchantCertificationService;

    /**
     * 获取商家列表
     */
    @GetMapping("/list")
    public R<Page<MerchantVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) Integer status) {

        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        // 如果有公司名称条件，先查询认证表
        List<Long> filteredUserIds = null;
        if (companyName != null && !companyName.isEmpty()) {
            LambdaQueryWrapper<MerchantCertification> certNameWrapper = new LambdaQueryWrapper<>();
            certNameWrapper.like(MerchantCertification::getCompanyName, companyName);
            List<MerchantCertification> certsByName = merchantCertificationService.list(certNameWrapper);

            if (certsByName.isEmpty()) {
                // 如果没有匹配的公司名称，直接返回空结果
                Page<MerchantVO> emptyResult = new Page<>(page, size, 0);
                emptyResult.setRecords(new ArrayList<>());
                return R.ok(emptyResult);
            }

            // 获取符合公司名称条件的用户ID
            filteredUserIds = certsByName.stream()
                    .map(MerchantCertification::getUserId)
                    .collect(Collectors.toList());
        }

        // 查询商家用户
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getRole, 1); // 商家角色

        // 如果有公司名称过滤，只查询这些用户ID
        if (filteredUserIds != null) {
            userWrapper.in(User::getId, filteredUserIds);
        }

        if (username != null && !username.isEmpty()) {
            userWrapper.like(User::getUsername, username);
        }

        if (nickname != null && !nickname.isEmpty()) {
            userWrapper.like(User::getNickname, nickname);
        }

        if (status != null) {
            userWrapper.eq(User::getStatus, status);
        }

        Page<User> userPage = userService.page(new Page<>(page, size), userWrapper);

        // 查询商家认证信息
        List<Long> userIds = userPage.getRecords().stream()
                .map(User::getId)
                .collect(Collectors.toList());

        List<MerchantCertification> certifications = new ArrayList<>();
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<MerchantCertification> certWrapper = new LambdaQueryWrapper<>();
            certWrapper.in(MerchantCertification::getUserId, userIds);
            certifications = merchantCertificationService.list(certWrapper);
        }

        // 将认证信息映射到用户ID
        Map<Long, MerchantCertification> certMap = certifications.stream()
                .collect(Collectors.toMap(MerchantCertification::getUserId, cert -> cert));

        // 构建返回结果
        List<MerchantVO> merchantList = userPage.getRecords().stream().map(user -> {
            MerchantVO merchant = new MerchantVO();
            merchant.setId(user.getId());
            merchant.setUsername(user.getUsername());
            merchant.setNickname(user.getNickname());
            merchant.setAvatar(user.getAvatar());
            merchant.setEmail(user.getEmail());
            merchant.setPhone(user.getPhone());
            merchant.setStatus(user.getStatus());
            merchant.setCreatedAt(user.getCreatedAt());

            // 添加认证信息
            MerchantCertification cert = certMap.get(user.getId());
            if (cert != null) {
                merchant.setCompanyName(cert.getCompanyName());
                merchant.setBusinessLicense(cert.getBusinessLicense());
                merchant.setContactName(cert.getContactName());
                merchant.setContactPhone(cert.getContactPhone());
                merchant.setContactEmail(cert.getContactEmail());
                merchant.setCertificationStatus(cert.getCertificationStatus());
                merchant.setCertifiedAt(cert.getUpdatedAt());
            }

            return merchant;
        }).collect(Collectors.toList());

        // 构建分页结果
        Page<MerchantVO> result = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        result.setRecords(merchantList);

        return R.ok(result);
    }

    /**
     * 获取商家详情
     */
    @GetMapping("/{id}")
    public R<MerchantVO> getDetail(@PathVariable Long id) {
        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        // 查询用户信息
        User user = userService.getById(id);
        if (user == null || user.getRole() != 1) {
            return R.failed("商家不存在");
        }

        // 查询认证信息
        MerchantCertification cert = merchantCertificationService.getByUserId(id);

        // 构建返回结果
        MerchantVO merchant = new MerchantVO();
        merchant.setId(user.getId());
        merchant.setUsername(user.getUsername());
        merchant.setNickname(user.getNickname());
        merchant.setAvatar(user.getAvatar());
        merchant.setEmail(user.getEmail());
        merchant.setPhone(user.getPhone());
        merchant.setStatus(user.getStatus());
        merchant.setCreatedAt(user.getCreatedAt());

        if (cert != null) {
            merchant.setCompanyName(cert.getCompanyName());
            merchant.setBusinessLicense(cert.getBusinessLicense());
            merchant.setContactName(cert.getContactName());
            merchant.setContactPhone(cert.getContactPhone());
            merchant.setContactEmail(cert.getContactEmail());
            merchant.setCertificationStatus(cert.getCertificationStatus());
            merchant.setCertifiedAt(cert.getUpdatedAt());
        }

        return R.ok(merchant);
    }

    /**
     * 更新商家状态
     */
    @PutMapping("/status")
    public R<Void> updateStatus(@RequestBody Map<String, Object> params) {
        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        Long id = Long.parseLong(params.get("id").toString());
        Integer status = Integer.parseInt(params.get("status").toString());

        // 查询用户信息
        User user = userService.getById(id);
        if (user == null || user.getRole() != 1) {
            return R.failed("商家不存在");
        }

        // 更新状态
        user.setStatus(status);
        userService.updateById(user);

        return R.ok();
    }
}
