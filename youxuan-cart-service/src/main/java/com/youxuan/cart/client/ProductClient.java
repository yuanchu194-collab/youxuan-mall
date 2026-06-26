package com.youxuan.cart.client;

import com.youxuan.cart.client.vo.ProductClientVO;
import com.youxuan.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 商品服务 Feign 客户端，用于购物车实时展示商品信息。
 */
@FeignClient(name = "youxuan-product-service")
public interface ProductClient {

    @GetMapping("/{id}")
    Result<ProductClientVO> detail(@PathVariable("id") Long id);
}
