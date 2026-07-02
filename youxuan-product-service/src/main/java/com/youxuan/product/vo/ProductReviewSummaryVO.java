package com.youxuan.product.vo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 商品评价汇总响应。
 */
public class ProductReviewSummaryVO {

    private BigDecimal avgRating;
    private Long reviewCount;
    private Map<Integer, Long> ratingCount;

    public BigDecimal getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(BigDecimal avgRating) {
        this.avgRating = avgRating;
    }

    public Long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Long reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Map<Integer, Long> getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Map<Integer, Long> ratingCount) {
        this.ratingCount = ratingCount;
    }
}
