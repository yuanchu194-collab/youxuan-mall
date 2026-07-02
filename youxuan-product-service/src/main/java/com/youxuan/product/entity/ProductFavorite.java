package com.youxuan.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youxuan.common.entity.BaseEntity;

/**
 * 商品收藏实体，对应 product_favorite 表。
 */
@TableName("product_favorite")
public class ProductFavorite extends BaseEntity {

    private Long userId;
    private Long productId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
