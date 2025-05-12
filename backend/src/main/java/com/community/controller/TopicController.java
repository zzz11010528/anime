package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.community.common.result.R;
import com.community.entity.Topic;
import com.community.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 话题控制器
 */
@RestController
@RequestMapping("/topic")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    /**
     * 获取话题列表
     */
    @GetMapping("/list")
    public R<List<Topic>> list() {
        // 只返回状态正常的话题
        return R.ok(topicService.list(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Topic>()
                .eq(Topic::getStatus, 1)
        ));
    }

    /**
     * 获取话题详情
     */
    @GetMapping("/{id}")
    public R<Topic> getById(@PathVariable Long id) {
        return R.ok(topicService.getById(id));
    }

    /**
     * 根据帖子ID获取话题列表
     */
    @GetMapping("/post/{postId}")
    public R<List<Topic>> getByPostId(@PathVariable Long postId) {
        return R.ok(topicService.getByPostId(postId));
    }

    /**
     * 添加话题（管理员接口）
     */
    @PostMapping
    public R<Void> add(@RequestBody Topic topic) {
        // 校验权限
        StpUtil.checkRole("admin");
        topicService.save(topic);
        return R.ok();
    }

    /**
     * 更新话题（管理员接口）
     */
    @PutMapping
    public R<Void> update(@RequestBody Topic topic) {
        // 校验权限
        StpUtil.checkRole("admin");
        topicService.updateById(topic);
        return R.ok();
    }

    /**
     * 更新话题状态（管理员接口）
     */
    @PutMapping("/status")
    public R<Void> updateStatus(@RequestBody Map<String, Object> params) {
        // 校验权限
        StpUtil.checkRole("admin");

        Long id = Long.parseLong(params.get("id").toString());
        Integer status = Integer.parseInt(params.get("status").toString());

        // 查询话题信息
        Topic topic = topicService.getById(id);
        if (topic == null) {
            return R.failed("话题不存在");
        }

        // 更新状态
        topic.setStatus(status);
        topicService.updateById(topic);

        return R.ok();
    }

    /**
     * 删除话题（管理员接口）
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        // 校验权限
        StpUtil.checkRole("admin");
        topicService.removeById(id);
        return R.ok();
    }
}
