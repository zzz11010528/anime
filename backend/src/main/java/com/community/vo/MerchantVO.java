package com.community.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家信息VO
 */
@Data
public class MerchantVO {
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 状态：0-禁用，1-正常
     */
    private Integer status;
    
    /**
     * 注册时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 公司名称
     */
    private String companyName;
    
    /**
     * 营业执照
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
     * 认证状态：0-待审核，1-已通过，2-已拒绝
     */
    private Integer certificationStatus;
    
    /**
     * 认证时间
     */
    private LocalDateTime certifiedAt;
}
