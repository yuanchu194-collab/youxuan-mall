package com.youxuan.product.service;

import com.youxuan.common.result.PageResult;
import com.youxuan.product.dto.ProductCreateDTO;
import com.youxuan.product.dto.ProductUpdateDTO;
import com.youxuan.product.dto.StockChangeDTO;
import com.youxuan.product.vo.ProductVO;
import java.util.List;

/**
 * 商品服务。
 */
public interface ProductService {

    ProductVO create(ProductCreateDTO createDTO);

    ProductVO update(Long id, ProductUpdateDTO updateDTO);

    void delete(Long id);

    ProductVO detail(Long id);

    PageResult<ProductVO> page(Long pageNum, Long pageSize, String name, Long categoryId, Integer status);

    void up(Long id);

    void down(Long id);

    ProductVO deductStock(StockChangeDTO stockChangeDTO);

    ProductVO restoreStock(StockChangeDTO stockChangeDTO);

    List<ProductVO> hotProducts(Integer limit);
}
