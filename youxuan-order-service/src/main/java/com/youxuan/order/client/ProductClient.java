package com.youxuan.order.client;

import com.youxuan.common.result.Result;
import com.youxuan.order.client.dto.StockChangeClientRequest;
import com.youxuan.order.client.vo.ProductClientVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 商品服务 Feign 客户端，用于订单确认页实时查询商品价格和库存。
 */
@FeignClient(name = "youxuan-product-service")
public interface ProductClient {

    @GetMapping("/{id}")
    Result<ProductClientVO> detail(@PathVariable("id") Long id);

    @PostMapping("/stock/deduct")
    Result<ProductClientVO> deductStock(@RequestBody StockChangeClientRequest request);

    @PostMapping("/stock/restore")
    Result<ProductClientVO> restoreStock(@RequestBody StockChangeClientRequest request);
}
