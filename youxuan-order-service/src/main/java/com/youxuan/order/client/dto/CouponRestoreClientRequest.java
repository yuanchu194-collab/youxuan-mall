package com.youxuan.order.client.dto;

/**
 * 调用优惠券服务恢复优惠券的请求。
 */
public class CouponRestoreClientRequest {

    private Long couponId;
    private Long orderId;

    public CouponRestoreClientRequest() {
    }

    public CouponRestoreClientRequest(Long couponId, Long orderId) {
        this.couponId = couponId;
        this.orderId = orderId;
    }

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
