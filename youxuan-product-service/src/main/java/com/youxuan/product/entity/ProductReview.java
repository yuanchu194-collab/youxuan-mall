package com.youxuan.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youxuan.common.entity.BaseEntity;

/**
 * 商品评价实体，对应 product_review 表。
 */
@TableName("product_review")
public class ProductReview extends BaseEntity {

    private Long productId;
    private Long userId;
    private String username;
    private Integer rating;
    private String content;

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
}
