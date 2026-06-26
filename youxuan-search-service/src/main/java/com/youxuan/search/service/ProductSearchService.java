package com.youxuan.search.service;

import com.youxuan.common.result.PageResult;
import com.youxuan.search.dto.ProductSearchRequest;
import com.youxuan.search.vo.ProductSearchVO;

/**
 * 商品搜索服务。
 */
public interface ProductSearchService {

    Integer importAll();

    PageResult<ProductSearchVO> search(ProductSearchRequest request);

    void syncProduct(Long productId);

    void deleteProductDocument(Long productId);
}
