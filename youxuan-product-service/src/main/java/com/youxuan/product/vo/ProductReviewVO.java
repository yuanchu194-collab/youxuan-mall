package com.youxuan.product.vo;

import com.youxuan.product.entity.ProductReview;
import java.time.LocalDateTime;

/**
 * 商品评价分页响应。
 */
public class ProductReviewVO {

    private Long id;
    private Long productId;
    private Long userId;
    private String username;
    private Integer rating;
    private String content;
    private LocalDateTime createTime;

    public static ProductReviewVO from(ProductReview review) {
        ProductReviewVO vo = new ProductReviewVO();
        vo.setId(review.getId());
        vo.setProductId(review.getProductId());
        vo.setUserId(review.getUserId());
        vo.setUsername(review.getUsername());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setCreateTime(review.getCreateTime());
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
