package com.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.result.R;
import com.community.entity.Comment;
import com.community.entity.Like;
import com.community.entity.Post;
import com.community.entity.User;
import com.community.mapper.CommentMapper;
import com.community.service.CommentService;
import com.community.service.LikeService;
import com.community.service.PostService;
import com.community.service.UserService;
import com.community.vo.CommentVO;
import com.community.vo.LikeVO;
import com.community.vo.PostVO;
import com.community.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cn.dev33.satoken.stp.StpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户个人中心控制器
 */
@RestController
@RequestMapping("/user/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final CommentService commentService;
    private final LikeService likeService;
    private final PostService postService;
    private final UserService userService;
    private final CommentMapper commentMapper;

    /**
     * 获取用户评论列表
     */
    @GetMapping("/comment/list")
    public R<Page<CommentVO>> getUserComments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 创建分页对象
        Page<Comment> pageParam = new Page<>(page, size);

        // 查询条件：用户ID和状态正常
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getUserId, userId);
        wrapper.eq(Comment::getStatus, 1);
        wrapper.orderByDesc(Comment::getCreatedAt);

        // 查询评论
        Page<Comment> commentPage = commentService.page(pageParam, wrapper);

        // 转换为VO
        Page<CommentVO> resultPage = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        List<CommentVO> commentVOList = new ArrayList<>();

        for (Comment comment : commentPage.getRecords()) {
            CommentVO commentVO = commentMapper.selectCommentDetail(comment.getId());
            if (commentVO != null) {
                commentVOList.add(commentVO);
            }
        }

        resultPage.setRecords(commentVOList);
        return R.ok(resultPage);
    }

    /**
     * 获取用户资料
     */
    @GetMapping("/{userId}")
    public R<UserProfileVO> getUserProfile(@PathVariable Long userId) {
        // 查询用户基本信息
        User user = userService.getById(userId);
        if (user == null) {
            return R.failed("用户不存在");
        }

        // 创建用户资料VO
        UserProfileVO profileVO = new UserProfileVO();
        profileVO.setId(user.getId());
        profileVO.setUsername(user.getUsername());
        profileVO.setNickname(user.getNickname());
        profileVO.setAvatar(user.getAvatar());
        profileVO.setGender(user.getGender());
        profileVO.setBio(user.getBio());
        profileVO.setCreatedAt(user.getCreatedAt());

        // 查询用户帖子数量
        LambdaQueryWrapper<Post> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.eq(Post::getUserId, userId);
        postWrapper.eq(Post::getStatus, 1); // 只统计正常状态的帖子
        int postCount = (int) postService.count(postWrapper);
        profileVO.setPostCount(postCount);

        // 查询用户评论数量
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getUserId, userId);
        commentWrapper.eq(Comment::getStatus, 1); // 只统计正常状态的评论
        int commentCount = (int) commentService.count(commentWrapper);
        profileVO.setCommentCount(commentCount);

        // 查询用户获赞数量
        LambdaQueryWrapper<Like> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(Like::getTargetId, userId);
        int likeCount = (int) likeService.count(likeWrapper);
        profileVO.setLikeCount(likeCount);

        return R.ok(profileVO);
    }

    /**
     * 获取用户点赞列表
     */
    @GetMapping("/like/list")
    public R<Page<LikeVO>> getUserLikes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "1") Integer type) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 创建分页对象
        Page<Like> pageParam = new Page<>(page, size);

        // 查询条件：用户ID和类型
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId);
        wrapper.eq(Like::getType, type);
        wrapper.orderByDesc(Like::getCreatedAt);

        // 查询点赞
        Page<Like> likePage = likeService.page(pageParam, wrapper);

        // 转换为VO
        Page<LikeVO> resultPage = new Page<>(likePage.getCurrent(), likePage.getSize(), likePage.getTotal());
        List<LikeVO> likeVOList = new ArrayList<>();

        for (Like like : likePage.getRecords()) {
            LikeVO likeVO = new LikeVO();
            likeVO.setId(like.getId());
            likeVO.setUserId(like.getUserId());
            likeVO.setTargetId(like.getTargetId());
            likeVO.setType(like.getType());
            likeVO.setCreatedAt(like.getCreatedAt());

            // 根据类型获取目标对象
            if (like.getType() == 1) {
                // 帖子
                PostVO postVO = postService.getDetail(like.getTargetId());
                if (postVO != null) {
                    likeVO.setTarget(postVO);
                }
            } else if (like.getType() == 2) {
                // 评论
                CommentVO commentVO = commentService.getDetail(like.getTargetId());
                if (commentVO != null) {
                    likeVO.setTarget(commentVO);
                }
            }

            likeVOList.add(likeVO);
        }

        resultPage.setRecords(likeVOList);
        return R.ok(resultPage);
    }
}
