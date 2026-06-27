package com.youxuan.home.client;

import com.youxuan.common.result.Result;
import com.youxuan.home.client.vo.ProductCategoryClientVO;
import com.youxuan.home.client.vo.ProductClientVO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商品服务 Feign 客户端，用于首页聚合分类、热门商品和推荐商品详情。
 */
@FeignClient(name = "youxuan-product-service")
public interface ProductClient {

    @GetMapping("/category/list")
    Result<List<ProductCategoryClientVO>> categories();

    @GetMapping("/home/hot")
    Result<List<ProductClientVO>> hotProducts(@RequestParam("limit") Integer limit);

    @GetMapping("/{id}")
    Result<ProductClientVO> detail(@PathVariable("id") Long id);
}
