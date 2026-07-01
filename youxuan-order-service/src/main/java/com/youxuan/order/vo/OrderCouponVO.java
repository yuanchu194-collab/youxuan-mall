package com.youxuan.order.vo;

import com.youxuan.order.client.vo.CouponClientVO;
import java.math.BigDecimal;

/**
 * 订单确认页优惠券展示。
 */
public class OrderCouponVO {

    private Long id;
    private Long couponId;
    private String name;
    private BigDecimal amount;
    private BigDecimal minAmount;
    private String scopeType;
    private Long categoryId;

    public static OrderCouponVO from(CouponClientVO coupon) {
        OrderCouponVO vo = new OrderCouponVO();
        vo.setId(coupon.getId());
        vo.setCouponId(coupon.getId());
        vo.setName(coupon.getName());
        vo.setAmount(coupon.getAmount());
        vo.setMinAmount(coupon.getMinAmount());
        vo.setScopeType(coupon.getScopeType());
        vo.setCategoryId(coupon.getCategoryId());
        return vo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
