package com.youxuan.product.client.vo;

/**
 * 接收优惠券服务返回的适用范围。
 */
public class CouponScopeClientVO {

    private Long id;
    private String name;
    private Integer status;
    private String scopeType;
    private Long categoryId;

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
