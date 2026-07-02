package com.youxuan.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youxuan.common.context.UserContext;
import com.youxuan.common.exception.BusinessException;
import com.youxuan.common.result.ErrorCode;
import com.youxuan.common.result.PageResult;
import com.youxuan.product.dto.ProductReviewCreateDTO;
import com.youxuan.product.entity.Product;
import com.youxuan.product.entity.ProductReview;
import com.youxuan.product.mapper.ProductMapper;
import com.youxuan.product.mapper.ProductReviewMapper;
import com.youxuan.product.service.ProductReviewService;
import com.youxuan.product.vo.ProductReviewSummaryVO;
import com.youxuan.product.vo.ProductReviewVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 商品评价服务实现，第一版只做商品维度评价。
 */
@Service
public class ProductReviewServiceImpl implements ProductReviewService {

    private static final int PRODUCT_ON = 1;
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;
    private static final int MAX_CONTENT_LENGTH = 500;

    private final ProductReviewMapper productReviewMapper;
    private final ProductMapper productMapper;

    public ProductReviewServiceImpl(ProductReviewMapper productReviewMapper, ProductMapper productMapper) {
        this.productReviewMapper = productReviewMapper;
        this.productMapper = productMapper;
    }

    @Override
    public PageResult<ProductReviewVO> page(Long productId, Long pageNum, Long pageSize) {
        validateProductExists(productId);
        long current = pageNum == null || pageNum < 1 ? 1L : pageNum;
        long size = pageSize == null || pageSize < 1 ? 10L : Math.min(pageSize, 50L);
        Page<ProductReview> page = productReviewMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<ProductReview>()
                        .eq(ProductReview::getProductId, productId)
                        .orderByDesc(ProductReview::getCreateTime)
                        .orderByDesc(ProductReview::getId));
        List<ProductReviewVO> records = page.getRecords().stream().map(ProductReviewVO::from).toList();
        return PageResult.of(page.getTotal(), current, size, records);
    }

    @Override
    public ProductReviewSummaryVO summary(Long productId) {
        validateProductExists(productId);
        List<ProductReview> reviews = productReviewMapper.selectList(new LambdaQueryWrapper<ProductReview>()
                .eq(ProductReview::getProductId, productId));
        Map<Integer, Long> ratingCount = new LinkedHashMap<>();
        for (int rating = MAX_RATING; rating >= MIN_RATING; rating--) {
            ratingCount.put(rating, 0L);
        }

        long totalScore = 0L;
        for (ProductReview review : reviews) {
            Integer rating = review.getRating();
            if (rating != null && rating >= MIN_RATING && rating <= MAX_RATING) {
                ratingCount.put(rating, ratingCount.get(rating) + 1);
                totalScore += rating;
            }
        }

        long reviewCount = reviews.size();
        ProductReviewSummaryVO summary = new ProductReviewSummaryVO();
        summary.setReviewCount(reviewCount);
        summary.setRatingCount(ratingCount);
        summary.setAvgRating(reviewCount == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(totalScore).divide(BigDecimal.valueOf(reviewCount), 1, RoundingMode.HALF_UP));
        return summary;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductReviewVO create(Long productId, ProductReviewCreateDTO createDTO) {
        validateCollectableProduct(productId);
        Long userId = currentUserId();
        validateReviewContent(createDTO);

        Long exists = productReviewMapper.selectCount(new LambdaQueryWrapper<ProductReview>()
                .eq(ProductReview::getProductId, productId)
                .eq(ProductReview::getUserId, userId));
        if (exists > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "已评价过该商品");
        }

        ProductReview review = new ProductReview();
        review.setProductId(productId);
        review.setUserId(userId);
        review.setUsername(resolveUsername(userId));
        review.setRating(createDTO.getRating());
        review.setContent(createDTO.getContent().trim());
        review.setDeleted(0);
        try {
            productReviewMapper.insert(review);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "已评价过该商品");
        }
        return ProductReviewVO.from(review);
    }

    private void validateReviewContent(ProductReviewCreateDTO createDTO) {
        if (createDTO.getRating() == null || createDTO.getRating() < MIN_RATING || createDTO.getRating() > MAX_RATING) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "评分必须在1到5之间");
        }
        if (!StringUtils.hasText(createDTO.getContent())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "评价内容不能为空");
        }
        if (createDTO.getContent().trim().length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "评价内容不能超过500字");
        }
    }

    private Product validateProductExists(Long productId) {
        validateProductId(productId);
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        return product;
    }

    private void validateCollectableProduct(Long productId) {
        Product product = validateProductExists(productId);
        if (!Integer.valueOf(PRODUCT_ON).equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "下架商品不能评价");
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

    private String resolveUsername(Long userId) {
        String username = UserContext.getUsername();
        return StringUtils.hasText(username) ? username : "用户" + userId;
    }
}
