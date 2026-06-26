package com.youxuan.product.service;

import com.youxuan.product.dto.ProductCategoryCreateDTO;
import com.youxuan.product.vo.ProductCategoryVO;
import java.util.List;

/**
 * 商品分类服务。
 */
public interface ProductCategoryService {

    ProductCategoryVO create(ProductCategoryCreateDTO createDTO);

    List<ProductCategoryVO> list();
}
