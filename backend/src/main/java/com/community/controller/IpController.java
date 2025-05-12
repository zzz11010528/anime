package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.community.common.result.R;
import com.community.entity.Ip;
import com.community.service.IpService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * IP控制器
 */
@RestController
@RequestMapping("/ip")
@RequiredArgsConstructor
public class IpController {

    private final IpService ipService;

    /**
     * 获取IP列表
     */
    @GetMapping("/list")
    public R<List<Ip>> list() {
        // 只返回状态正常的IP
        return R.ok(ipService.list(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Ip>()
                .eq(Ip::getStatus, 1)
        ));
    }

    /**
     * 获取IP详情
     */
    @GetMapping("/{id}")
    public R<Ip> getById(@PathVariable Long id) {
        return R.ok(ipService.getById(id));
    }

    /**
     * 添加IP（管理员接口）
     */
    @PostMapping
    public R<Void> add(@RequestBody Ip ip) {
        // 校验权限
        StpUtil.checkRole("admin");
        ipService.save(ip);
        return R.ok();
    }

    /**
     * 更新IP（管理员接口）
     */
    @PutMapping
    public R<Void> update(@RequestBody Ip ip) {
        // 校验权限
        StpUtil.checkRole("admin");
        ipService.updateById(ip);
        return R.ok();
    }

    /**
     * 更新IP状态（管理员接口）
     */
    @PutMapping("/status")
    public R<Void> updateStatus(@RequestBody Map<String, Object> params) {
        // 校验权限
        StpUtil.checkRole("admin");

        Long id = Long.parseLong(params.get("id").toString());
        Integer status = Integer.parseInt(params.get("status").toString());

        // 查询IP信息
        Ip ip = ipService.getById(id);
        if (ip == null) {
            return R.failed("IP不存在");
        }

        // 更新状态
        ip.setStatus(status);
        ipService.updateById(ip);

        return R.ok();
    }

    /**
     * 删除IP（管理员接口）
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        // 校验权限
        StpUtil.checkRole("admin");
        ipService.removeById(id);
        return R.ok();
    }
}
