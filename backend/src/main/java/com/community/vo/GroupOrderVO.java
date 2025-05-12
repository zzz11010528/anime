package com.community.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 拼团订单VO
 */
@Data
public class GroupOrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 拼团订单ID
     */
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
     * 团长用户名
     */
    private String leaderUsername;

    /**
     * 团长昵称
     */
    private String leaderNickname;

    /**
     * 团长头像
     */
    private String leaderAvatar;

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
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 商品原价
     */
    private BigDecimal productPrice;

    /**
     * 拼团价格
     */
    private BigDecimal groupPrice;

    /**
     * 最小成团人数
     */
    private Integer minGroupSize;

    /**
     * 参与用户的订单列表
     */
    private List<OrderVO> orders;
}
