package com.youxuan.coupon.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 更新优惠券请求。已被用户领取的优惠券会在服务层限制敏感字段变更。
 */
public class CouponUpdateRequest {

    @Size(max = 128, message = "优惠券名称不能超过128个字符")
    private String name;

    @DecimalMin(value = "0.01", message = "优惠金额必须大于0")
    private BigDecimal amount;

    @DecimalMin(value = "0.00", message = "使用门槛不能小于0")
    private BigDecimal minAmount;

    @Min(value = 0, message = "总库存不能小于0")
    private Integer totalStock;

    @Min(value = 0, message = "剩余库存不能小于0")
    private Integer availableStock;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    @Min(value = 0, message = "优惠券状态只能是0或1")
    @Max(value = 1, message = "优惠券状态只能是0或1")
    private Integer status;
    private String scopeType;
    private Long categoryId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public Integer getTotalStock() { return totalStock; }
    public void setTotalStock(Integer totalStock) { this.totalStock = totalStock; }
    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
