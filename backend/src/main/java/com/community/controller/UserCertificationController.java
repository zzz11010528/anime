package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.community.common.result.R;
import com.community.entity.MerchantCertification;
import com.community.service.MerchantCertificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器 - 普通用户也可以访问的认证接口
 */
@RestController
@RequestMapping("/user/certification")
@RequiredArgsConstructor
public class UserCertificationController {

    private final MerchantCertificationService merchantCertificationService;

    /**
     * 申请商家认证 - 普通用户可访问
     */
    @PostMapping("/apply")
    public R<Void> apply(@RequestBody MerchantCertification certification) {
        // 设置用户ID
        certification.setUserId(StpUtil.getLoginIdAsLong());
        merchantCertificationService.apply(certification);
        return R.ok();
    }

    /**
     * 获取认证信息 - 普通用户可访问
     */
    @GetMapping("/info")
    public R<MerchantCertification> getInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        MerchantCertification certification = merchantCertificationService.getByUserId(userId);
        return R.ok(certification);
    }
}
