package com.youxuan.coupon.vo;

import com.youxuan.coupon.entity.Coupon;

/**
 * 优惠券适用范围响应，用于商品列表筛选和订单侧复核。
 */
public class CouponScopeVO {

    private Long id;
    private String name;
    private Integer status;
    private String scopeType;
    private Long categoryId;

    public static CouponScopeVO from(Coupon coupon) {
        CouponScopeVO vo = new CouponScopeVO();
        vo.setId(coupon.getId());
        vo.setName(coupon.getName());
        vo.setStatus(coupon.getStatus());
        vo.setScopeType(coupon.getScopeType());
        vo.setCategoryId(coupon.getCategoryId());
        return vo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
