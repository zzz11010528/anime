package com.community.vo;

import com.community.entity.PostImage;
import com.community.entity.Topic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子VO
 */
@Data
public class PostVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 帖子ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * IP ID
     */
    private Long ipId;

    /**
     * IP名称
     */
    private String ipName;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    /**
     * 状态：0-删除，1-正常，2-审核中
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 帖子图片列表
     */
    private List<PostImage> images;

    /**
     * 话题列表
     */
    private List<Topic> topics;

    /**
     * 是否点赞
     */
    private Boolean isLiked;

    /**
     * 是否收藏
     */
    private Boolean isCollected;
}
