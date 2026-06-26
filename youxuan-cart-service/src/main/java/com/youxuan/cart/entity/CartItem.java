package com.youxuan.cart.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youxuan.common.entity.BaseEntity;

/**
 * 购物车项实体，只保存用户、商品、数量和勾选状态。
 */
@TableName("cart_item")
public class CartItem extends BaseEntity {

    private Long userId;
    private Long productId;
    private Integer quantity;
    private Integer checked;

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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getChecked() {
        return checked;
    }

    public void setChecked(Integer checked) {
        this.checked = checked;
    }
}
