package com.youxuan.product.service;

import com.youxuan.common.result.PageResult;
import com.youxuan.product.vo.FavoriteProductVO;
import java.util.List;
import java.util.Set;

/**
 * 商品收藏服务。
 */
public interface ProductFavoriteService {

    void collect(Long productId);

    void cancel(Long productId);

    PageResult<FavoriteProductVO> page(Long pageNum, Long pageSize);

    Boolean check(Long productId);

    Set<Long> checkBatch(List<Long> productIds);
}
