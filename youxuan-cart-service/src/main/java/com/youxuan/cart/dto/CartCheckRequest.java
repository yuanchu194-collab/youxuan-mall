package com.youxuan.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 勾选或取消勾选单个购物车项请求。
 */
public class CartCheckRequest {

    @NotNull(message = "购物车项ID不能为空")
    private Long cartItemId;

    @NotNull(message = "勾选状态不能为空")
    @Min(value = 0, message = "勾选状态只能是0或1")
    @Max(value = 1, message = "勾选状态只能是0或1")
    private Integer checked;

    public Long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Integer getChecked() {
        return checked;
    }

    public void setChecked(Integer checked) {
        this.checked = checked;
    }
}
