package com.youxuan.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youxuan.product.entity.ProductReview;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价数据访问接口。
 */
@Mapper
public interface ProductReviewMapper extends BaseMapper<ProductReview> {
}
