package com.youxuan.search.document;

import com.youxuan.search.client.vo.ProductClientVO;
import java.math.BigDecimal;

/**
 * 写入 youxuan_product 索引的商品文档。
 */
public class ProductDocument {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String subtitle;
    private String mainImage;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer sales;
    private Integer status;
    private String createTime;

    public static ProductDocument from(ProductClientVO product) {
        ProductDocument document = new ProductDocument();
        document.setId(product.getId());
        document.setCategoryId(product.getCategoryId());
        document.setCategoryName(product.getCategoryName());
        document.setName(product.getName());
        document.setSubtitle(product.getSubtitle());
        document.setMainImage(product.getMainImage());
        document.setPrice(product.getPrice());
        document.setOriginalPrice(product.getOriginalPrice());
        document.setSales(product.getSales());
        document.setStatus(product.getStatus());
        document.setCreateTime(product.getCreateTime() == null ? null : product.getCreateTime().toString());
        return document;
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

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
