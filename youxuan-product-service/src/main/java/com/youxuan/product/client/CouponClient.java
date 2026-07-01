package com.youxuan.product.client;

import com.youxuan.common.result.Result;
import com.youxuan.product.client.vo.CouponScopeClientVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 优惠券服务 Feign 客户端，用于商品分页按优惠券适用范围过滤。
 */
@FeignClient(name = "youxuan-coupon-service")
public interface CouponClient {

    @GetMapping("/{id}/scope")
    Result<CouponScopeClientVO> scope(@PathVariable("id") Long id);
}
