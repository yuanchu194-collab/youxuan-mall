package com.youxuan.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youxuan.common.context.UserContext;
import com.youxuan.common.exception.BusinessException;
import com.youxuan.common.result.ErrorCode;
import com.youxuan.common.result.PageResult;
import com.youxuan.product.entity.Product;
import com.youxuan.product.entity.ProductFavorite;
import com.youxuan.product.mapper.ProductFavoriteMapper;
import com.youxuan.product.mapper.ProductMapper;
import com.youxuan.product.service.ProductFavoriteService;
import com.youxuan.product.vo.FavoriteProductVO;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品收藏服务实现，收藏关系只使用当前登录用户上下文，不接收前端 userId。
 */
@Service
public class ProductFavoriteServiceImpl implements ProductFavoriteService {

    private static final int PRODUCT_ON = 1;

    private final ProductFavoriteMapper productFavoriteMapper;
    private final ProductMapper productMapper;

    public ProductFavoriteServiceImpl(ProductFavoriteMapper productFavoriteMapper, ProductMapper productMapper) {
        this.productFavoriteMapper = productFavoriteMapper;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collect(Long productId) {
        Long userId = currentUserId();
        validateCollectableProduct(productId);

        ProductFavorite existing = productFavoriteMapper.selectByUserIdAndProductIdIncludingDeleted(userId, productId);
        if (existing != null) {
            if (Integer.valueOf(1).equals(existing.getDeleted())) {
                productFavoriteMapper.restoreById(existing.getId());
            }
            return;
        }

        ProductFavorite favorite = new ProductFavorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favorite.setDeleted(0);
        try {
            productFavoriteMapper.insert(favorite);
        } catch (DuplicateKeyException exception) {
            ProductFavorite duplicated = productFavoriteMapper.selectByUserIdAndProductIdIncludingDeleted(userId, productId);
            if (duplicated != null && Integer.valueOf(1).equals(duplicated.getDeleted())) {
                productFavoriteMapper.restoreById(duplicated.getId());
                return;
            }
            throw exception;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long productId) {
        Long userId = currentUserId();
        validateProductId(productId);
        ProductFavorite favorite = productFavoriteMapper.selectOne(new LambdaQueryWrapper<ProductFavorite>()
                .eq(ProductFavorite::getUserId, userId)
                .eq(ProductFavorite::getProductId, productId)
                .last("LIMIT 1"));
        if (favorite != null) {
            productFavoriteMapper.deleteById(favorite.getId());
        }
    }

    @Override
    public PageResult<FavoriteProductVO> page(Long pageNum, Long pageSize) {
        long current = pageNum == null || pageNum < 1 ? 1L : pageNum;
        long size = pageSize == null || pageSize < 1 ? 10L : Math.min(pageSize, 50L);
        Page<FavoriteProductVO> page = productFavoriteMapper.selectFavoritePage(new Page<>(current, size), currentUserId());
        return PageResult.of(page.getTotal(), current, size, page.getRecords());
    }

    @Override
    public Boolean check(Long productId) {
        Long userId = currentUserId();
        validateProductId(productId);
        return productFavoriteMapper.selectCount(new LambdaQueryWrapper<ProductFavorite>()
                .eq(ProductFavorite::getUserId, userId)
                .eq(ProductFavorite::getProductId, productId)) > 0;
    }

    @Override
    public Set<Long> checkBatch(List<Long> productIds) {
        Long userId = UserContext.getUserId();
        if (userId == null || productIds == null || productIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> safeProductIds = productIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .limit(100)
                .toList();
        if (safeProductIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new LinkedHashSet<>(productFavoriteMapper.selectCollectedProductIds(userId, safeProductIds));
    }

    private void validateCollectableProduct(Long productId) {
        validateProductId(productId);
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        if (!Integer.valueOf(PRODUCT_ON).equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "下架商品不能收藏");
        }
    }

    private void validateProductId(Long productId) {
        if (productId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "商品ID不能为空");
        }
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return userId;
    }
}
