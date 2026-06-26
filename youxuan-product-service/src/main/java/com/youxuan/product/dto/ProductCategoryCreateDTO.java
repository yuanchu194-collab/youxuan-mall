package com.youxuan.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建商品分类请求。
 */
public class ProductCategoryCreateDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称不能超过64个字符")
    private String name;

    @Min(value = 0, message = "父分类ID不能小于0")
    private Long parentId = 0L;

    @Min(value = 0, message = "排序不能小于0")
    private Integer sort = 0;

    @Min(value = 0, message = "分类状态只能是0或1")
    @Max(value = 1, message = "分类状态只能是0或1")
    private Integer status = 1;

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
