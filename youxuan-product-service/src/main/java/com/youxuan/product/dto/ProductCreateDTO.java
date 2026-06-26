package com.youxuan.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 创建商品请求，包含首批库存数量。
 */
public class ProductCreateDTO {

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 128, message = "商品名称不能超过128个字符")
    private String name;

    @Size(max = 255, message = "副标题不能超过255个字符")
    private String subtitle;

    @Size(max = 255, message = "主图URL不能超过255个字符")
    private String mainImage;

    private String detail;

    @NotNull(message = "商品售价不能为空")
    @DecimalMin(value = "0.01", message = "商品售价必须大于0")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "商品原价必须大于0")
    private BigDecimal originalPrice;

    @Min(value = 0, message = "销量不能小于0")
    private Integer sales = 0;

    @Min(value = 0, message = "商品状态只能是0或1")
    @Max(value = 1, message = "商品状态只能是0或1")
    private Integer status = 1;

    @NotNull(message = "初始库存不能为空")
    @Min(value = 0, message = "初始库存不能小于0")
    private Integer stock;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
