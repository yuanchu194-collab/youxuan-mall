package com.youxuan.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youxuan.common.entity.BaseEntity;

/**
 * 商品分类实体，对应 product_category 表。
 */
@TableName("product_category")
public class ProductCategory extends BaseEntity {

    private String name;
    private Long parentId;
    private Integer sort;
    private Integer status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
