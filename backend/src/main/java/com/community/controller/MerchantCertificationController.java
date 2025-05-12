package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.community.common.result.R;
import com.community.entity.MerchantCertification;
import com.community.service.MerchantCertificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商家认证控制器
 */
@RestController
@RequestMapping("/merchant/certification")
@RequiredArgsConstructor
public class MerchantCertificationController {

    private final MerchantCertificationService merchantCertificationService;

    /**
     * 申请商家认证
     */
    @PostMapping("/apply")
    public R<Void> apply(@RequestBody MerchantCertification certification) {
        // 设置用户ID
        certification.setUserId(StpUtil.getLoginIdAsLong());
        merchantCertificationService.apply(certification);
        return R.ok();
    }

    /**
     * 获取认证信息
     */
    @GetMapping("/info")
    public R<MerchantCertification> getInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        MerchantCertification certification = merchantCertificationService.getByUserId(userId);
        return R.ok(certification);
    }

    /**
     * 审核商家认证（管理员接口）
     */
    @PostMapping("/audit")
    public R<Void> audit(@RequestBody Map<String, Object> params) {
        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        // 从请求体中获取参数
        if (params.get("id") == null) {
            return R.failed("认证ID不能为空");
        }

        if (params.get("status") == null) {
            return R.failed("认证状态不能为空");
        }

        Long id = Long.parseLong(params.get("id").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        String rejectReason = params.get("rejectReason") != null ? params.get("rejectReason").toString() : null;

        merchantCertificationService.audit(id, status, rejectReason);
        return R.ok();
    }

    /**
     * 获取认证列表（管理员接口）
     */
    @GetMapping("/list")
    public R<Object> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String contactName,
            @RequestParam(required = false) Integer status) {
        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        return R.ok(merchantCertificationService.getCertificationList(page, size, companyName, contactName, status));
    }
}
