package com.youxuan.order.client.vo;

import java.math.BigDecimal;

/**
 * 接收优惠券服务可用优惠券返回的数据。
 */
public class CouponClientVO {

    private Long id;
    private String name;
    private BigDecimal amount;
    private BigDecimal minAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
}
