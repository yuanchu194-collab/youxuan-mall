package com.youxuan.coupon.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 优惠券恢复请求，用于订单创建失败后的基础补偿。
 */
public class CouponRestoreRequest {

    @NotNull(message = "优惠券ID不能为空")
    private Long couponId;

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
