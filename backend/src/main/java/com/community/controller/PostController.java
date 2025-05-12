package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.result.R;
import com.community.entity.Post;
import com.community.entity.PostImage;
import com.community.service.PostService;
import com.community.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 帖子控制器
 */
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 分页查询帖子列表
     */
    @GetMapping("/page")
    public R<Page<PostVO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long ipId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "created_at") String orderBy,
            @RequestParam(defaultValue = "false") Boolean isAsc) {

        // 判断当前用户是否为管理员
        boolean isAdmin = false;
        try {
            isAdmin = StpUtil.hasRole("admin");
        } catch (Exception e) {
            // 未登录或token无效，视为普通用户
        }

        // 如果不是管理员且未指定状态，则只显示正常状态的帖子
        if (!isAdmin && status == null) {
            status = 1; // 正常状态
        }

        return R.ok(postService.page(page, size, categoryId, ipId, keyword, userId, status, orderBy, isAsc));
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/{id}")
    public R<PostVO> getDetail(@PathVariable Long id) {
        PostVO postVO = postService.getDetail(id);

        // 如果帖子不存在，直接返回
        if (postVO == null) {
            return R.ok(null);
        }

        // 判断当前用户是否为管理员或帖子作者
        boolean isAdminOrAuthor = false;
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            isAdminOrAuthor = StpUtil.hasRole("admin") || userId.equals(postVO.getUserId());
        } catch (Exception e) {
            // 未登录或token无效，视为普通用户
        }

        // 如果不是管理员或作者，且帖子状态不是正常，则不允许查看
        if (!isAdminOrAuthor && postVO.getStatus() != 1) {
            return R.failed("帖子不存在或正在审核中");
        }

        return R.ok(postVO);
    }

    /**
     * 发布帖子
     */
    @PostMapping
    public R<Long> publish(@RequestBody Map<String, Object> params) {
        Post post = new Post();
        List<PostImage> images = null;
        List<Long> topicIds = null;

        // 设置用户ID
        post.setUserId(StpUtil.getLoginIdAsLong());

        // 解析帖子基本信息
        if (params.containsKey("title")) {
            post.setTitle((String) params.get("title"));
        }
        if (params.containsKey("content")) {
            post.setContent((String) params.get("content"));
        }
        if (params.containsKey("categoryId")) {
            post.setCategoryId(Long.valueOf(params.get("categoryId").toString()));
        }
        if (params.containsKey("ipId")) {
            post.setIpId(Long.valueOf(params.get("ipId").toString()));
        }

        // 解析图片信息
        if (params.containsKey("images") && params.get("images") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> imageList = (List<Map<String, Object>>) params.get("images");
            if (!imageList.isEmpty()) {
                images = new java.util.ArrayList<>();
                for (int i = 0; i < imageList.size(); i++) {
                    Map<String, Object> imageMap = imageList.get(i);
                    PostImage image = new PostImage();
                    image.setImageUrl((String) imageMap.get("imageUrl"));
                    image.setSort(i + 1);
                    images.add(image);
                }
            }
        }

        // 解析话题信息
        if (params.containsKey("topicIds") && params.get("topicIds") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> topicList = (List<Object>) params.get("topicIds");
            if (!topicList.isEmpty()) {
                topicIds = topicList.stream()
                        .map(id -> Long.valueOf(id.toString()))
                        .collect(java.util.stream.Collectors.toList());
            }
        }

        Long postId = postService.publish(post, images, topicIds);
        return R.ok(postId);
    }

    /**
     * 更新帖子
     */
    @PutMapping
    public R<Void> update(@RequestBody Map<String, Object> params) {
        Post post = new Post();
        List<PostImage> images = null;
        List<Long> topicIds = null;

        // 校验是否有权限修改
        Long userId = StpUtil.getLoginIdAsLong();
        post.setUserId(userId);

        // 解析帖子ID
        if (params.containsKey("id")) {
            post.setId(Long.valueOf(params.get("id").toString()));
        } else {
            return R.failed("帖子ID不能为空");
        }

        // 解析帖子基本信息
        if (params.containsKey("title")) {
            post.setTitle((String) params.get("title"));
        }
        if (params.containsKey("content")) {
            post.setContent((String) params.get("content"));
        }
        if (params.containsKey("categoryId")) {
            post.setCategoryId(Long.valueOf(params.get("categoryId").toString()));
        }
        if (params.containsKey("ipId")) {
            post.setIpId(Long.valueOf(params.get("ipId").toString()));
        }

        // 解析图片信息
        if (params.containsKey("images") && params.get("images") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> imageList = (List<Map<String, Object>>) params.get("images");
            if (!imageList.isEmpty()) {
                images = new java.util.ArrayList<>();
                for (int i = 0; i < imageList.size(); i++) {
                    Map<String, Object> imageMap = imageList.get(i);
                    PostImage image = new PostImage();
                    image.setImageUrl((String) imageMap.get("imageUrl"));
                    image.setSort(i + 1);
                    images.add(image);
                }
            }
        }

        // 解析话题信息
        if (params.containsKey("topicIds") && params.get("topicIds") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> topicList = (List<Object>) params.get("topicIds");
            if (!topicList.isEmpty()) {
                topicIds = topicList.stream()
                        .map(id -> Long.valueOf(id.toString()))
                        .collect(java.util.stream.Collectors.toList());
            }
        }

        postService.update(post, images, topicIds);
        return R.ok();
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        // 校验是否有权限删除
        Post post = postService.getById(id);
        if (post == null) {
            return R.failed("帖子不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (!post.getUserId().equals(userId) && !StpUtil.hasRole("admin")) {
            return R.failed("无权操作");
        }

        postService.delete(id);
        return R.ok();
    }

    /**
     * 审核帖子（管理员接口）
     */
    @PostMapping("/audit")
    public R<Void> audit(@RequestParam Long id, @RequestParam Integer status, @RequestParam(required = false) String rejectReason) {
        // 校验权限
        StpUtil.checkRole("admin");

        // 如果是拒绝状态且没有拒绝原因，返回错误
        if (status == 0 && (rejectReason == null || rejectReason.isEmpty())) {
            return R.failed("拒绝原因不能为空");
        }

        postService.audit(id, status, rejectReason);
        return R.ok();
    }

    /**
     * 获取用户发布的帖子列表
     */
    @GetMapping("/user")
    public R<Page<PostVO>> getUserPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(postService.getUserPosts(userId, page, size));
    }
}
