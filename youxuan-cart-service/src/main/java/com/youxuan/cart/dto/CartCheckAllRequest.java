package com.youxuan.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 全选或取消全选请求。
 */
public class CartCheckAllRequest {

    @NotNull(message = "勾选状态不能为空")
    @Min(value = 0, message = "勾选状态只能是0或1")
    @Max(value = 1, message = "勾选状态只能是0或1")
    private Integer checked;

    public Integer getChecked() {
        return checked;
    }

    public void setChecked(Integer checked) {
        this.checked = checked;
    }
}
