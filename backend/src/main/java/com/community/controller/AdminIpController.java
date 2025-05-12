package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.community.common.result.R;
import com.community.entity.Ip;
import com.community.service.IpService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员-IP控制器
 */
@RestController
@RequestMapping("/admin/ip")
@RequiredArgsConstructor
public class AdminIpController {

    private final IpService ipService;

    /**
     * 获取所有IP列表（包括禁用的）
     */
    @GetMapping("/list")
    public R<List<Ip>> list() {
        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        // 返回所有IP，包括禁用的
        return R.ok(ipService.list());
    }
}
