package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.result.R;
import com.community.entity.Topic;
import com.community.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员-话题控制器
 */
@RestController
@RequestMapping("/admin/topic")
@RequiredArgsConstructor
public class AdminTopicController {

    private final TopicService topicService;

    /**
     * 获取所有话题列表（包括禁用的）
     */
    @GetMapping("/list")
    public R<List<Topic>> list() {
        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        // 返回所有话题，包括禁用的
        return R.ok(topicService.list());
    }
}
