package com.youxuan.search.client;

import com.youxuan.common.result.PageResult;
import com.youxuan.common.result.Result;
import com.youxuan.search.client.vo.ProductClientVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商品服务 Feign 客户端，阶段 8 只用于手动全量导入 ES。
 */
@FeignClient(name = "youxuan-product-service")
public interface ProductClient {

    @GetMapping("/page")
    Result<PageResult<ProductClientVO>> page(@RequestParam(name = "pageNum") Long pageNum,
                                             @RequestParam(name = "pageSize") Long pageSize);

    @GetMapping("/{id}")
    Result<ProductClientVO> detail(@PathVariable("id") Long id);
}
