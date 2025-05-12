package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.ProductDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品详情Mapper接口
 */
@Mapper
public interface ProductDetailMapper extends BaseMapper<ProductDetail> {
}
