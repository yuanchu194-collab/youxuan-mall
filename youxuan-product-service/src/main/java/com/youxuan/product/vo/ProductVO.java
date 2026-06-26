package com.youxuan.product.vo;

import com.youxuan.product.entity.Product;
import com.youxuan.product.entity.ProductCategory;
import com.youxuan.product.entity.ProductStock;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品响应，包含分类和库存展示字段。
 */
public class ProductVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String subtitle;
    private String mainImage;
    private String detail;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer sales;
    private Integer status;
    private Integer stock;
    private Integer lockedStock;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static ProductVO from(Product product, ProductCategory category, ProductStock stock) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setCategoryId(product.getCategoryId());
        vo.setCategoryName(category == null ? null : category.getName());
        vo.setName(product.getName());
        vo.setSubtitle(product.getSubtitle());
        vo.setMainImage(product.getMainImage());
        vo.setDetail(product.getDetail());
        vo.setPrice(product.getPrice());
        vo.setOriginalPrice(product.getOriginalPrice());
        vo.setSales(product.getSales());
        vo.setStatus(product.getStatus());
        vo.setStock(stock == null ? 0 : stock.getStock());
        vo.setLockedStock(stock == null ? 0 : stock.getLockedStock());
        vo.setCreateTime(product.getCreateTime());
        vo.setUpdateTime(product.getUpdateTime());
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public Integer getLockedStock() {
        return lockedStock;
    }

    public void setLockedStock(Integer lockedStock) {
        this.lockedStock = lockedStock;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
