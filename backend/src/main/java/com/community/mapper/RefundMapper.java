package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.Refund;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退款信息Mapper接口
 */
@Mapper
public interface RefundMapper extends BaseMapper<Refund> {
}
