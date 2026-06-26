package com.youxuan.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youxuan.product.entity.ProductStock;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存数据访问接口。
 */
@Mapper
public interface ProductStockMapper extends BaseMapper<ProductStock> {
}
