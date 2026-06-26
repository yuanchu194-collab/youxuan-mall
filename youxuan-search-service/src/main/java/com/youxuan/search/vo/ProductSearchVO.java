package com.youxuan.search.vo;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;

/**
 * 商品搜索结果响应。
 */
public class ProductSearchVO {

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

    public static ProductSearchVO fromSource(JsonNode source) {
        ProductSearchVO vo = new ProductSearchVO();
        vo.setId(source.path("id").asLong());
        vo.setCategoryId(source.path("categoryId").isMissingNode() || source.path("categoryId").isNull()
                ? null : source.path("categoryId").asLong());
        vo.setCategoryName(source.path("categoryName").asText(null));
        vo.setName(source.path("name").asText(null));
        vo.setSubtitle(source.path("subtitle").asText(null));
        vo.setMainImage(source.path("mainImage").asText(null));
        vo.setPrice(toBigDecimal(source.path("price")));
        vo.setOriginalPrice(toBigDecimal(source.path("originalPrice")));
        vo.setSales(source.path("sales").isMissingNode() || source.path("sales").isNull()
                ? 0 : source.path("sales").asInt());
        vo.setStatus(source.path("status").isMissingNode() || source.path("status").isNull()
                ? null : source.path("status").asInt());
        vo.setCreateTime(source.path("createTime").asText(null));
        return vo;
    }

    private static BigDecimal toBigDecimal(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        return new BigDecimal(node.asText());
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
