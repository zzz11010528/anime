package com.community.vo;

import com.community.entity.MerchantCertification;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家认证VO（包含用户信息）
 */
@Data
public class MerchantCertificationVO {

    /**
     * 认证ID
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
     * 昵称
     */
    private String nickname;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 营业执照URL
     */
    private String businessLicense;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 联系人邮箱
     */
    private String contactEmail;

    /**
     * 认证状态：0-审核中，1-已认证，2-未通过
     */
    private Integer certificationStatus;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
