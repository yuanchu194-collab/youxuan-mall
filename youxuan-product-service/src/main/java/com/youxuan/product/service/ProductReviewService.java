package com.youxuan.product.service;

import com.youxuan.common.result.PageResult;
import com.youxuan.product.dto.ProductReviewCreateDTO;
import com.youxuan.product.vo.ProductReviewSummaryVO;
import com.youxuan.product.vo.ProductReviewVO;

/**
 * 商品评价服务。
 */
public interface ProductReviewService {

    PageResult<ProductReviewVO> page(Long productId, Long pageNum, Long pageSize);

    ProductReviewSummaryVO summary(Long productId);

    ProductReviewVO create(Long productId, ProductReviewCreateDTO createDTO);
}
