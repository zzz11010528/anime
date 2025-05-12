package com.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 拼团订单实体类
 */
@Data
@TableName("group_order")
public class GroupOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 拼团订单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 拼团活动ID
     */
    private Long groupBuyingId;

    /**
     * 团长用户ID
     */
    private Long leaderUserId;

    /**
     * 当前人数
     */
    private Integer currentSize;

    /**
     * 状态：0-组团中，1-已成团，2-已解散
     */
    private Integer status;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
