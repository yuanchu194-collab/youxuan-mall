package com.youxuan.order.vo;

import com.youxuan.order.client.vo.CouponClientVO;
import java.math.BigDecimal;

/**
 * 订单确认页优惠券展示。
 */
public class OrderCouponVO {

    private Long couponId;
    private String name;
    private BigDecimal amount;
    private BigDecimal minAmount;

    public static OrderCouponVO from(CouponClientVO coupon) {
        OrderCouponVO vo = new OrderCouponVO();
        vo.setCouponId(coupon.getId());
        vo.setName(coupon.getName());
        vo.setAmount(coupon.getAmount());
        vo.setMinAmount(coupon.getMinAmount());
        return vo;
    }

    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
}
