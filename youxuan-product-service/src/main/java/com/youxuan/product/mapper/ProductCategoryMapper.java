package com.youxuan.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youxuan.product.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类数据访问接口。
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
}
