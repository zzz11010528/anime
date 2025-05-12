package com.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.MerchantCertification;
import com.community.vo.MerchantCertificationVO;

/**
 * 商家认证服务接口
 */
public interface MerchantCertificationService extends IService<MerchantCertification> {

    /**
     * 申请商家认证
     * @param certification 认证信息
     * @return 是否成功
     */
    boolean apply(MerchantCertification certification);

    /**
     * 审核商家认证
     * @param id 认证ID
     * @param status 认证状态：1-通过，2-拒绝
     * @param rejectReason 拒绝原因
     * @return 是否成功
     */
    boolean audit(Long id, Integer status, String rejectReason);

    /**
     * 获取用户的认证信息
     * @param userId 用户ID
     * @return 认证信息
     */
    MerchantCertification getByUserId(Long userId);

    /**
     * 获取认证列表（包含用户信息）
     * @param page 页码
     * @param size 每页大小
     * @param companyName 公司名称
     * @param contactName 联系人
     * @param status 认证状态
     * @return 认证列表
     */
    Page<MerchantCertificationVO> getCertificationList(Integer page, Integer size, String companyName, String contactName, Integer status);
}
