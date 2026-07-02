package com.youxuan.product.controller;

import com.youxuan.common.result.PageResult;
import com.youxuan.common.result.Result;
import com.youxuan.product.service.ProductFavoriteService;
import com.youxuan.product.vo.FavoriteProductVO;
import java.util.List;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品收藏接口。
 */
@RestController
@RequestMapping(value = "/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductFavoriteController {

    private final ProductFavoriteService productFavoriteService;

    public ProductFavoriteController(ProductFavoriteService productFavoriteService) {
        this.productFavoriteService = productFavoriteService;
    }

    @PostMapping("/{productId:\\d+}")
    public Result<Void> collect(@PathVariable("productId") Long productId) {
        productFavoriteService.collect(productId);
        return Result.success();
    }

    @DeleteMapping("/{productId:\\d+}")
    public Result<Void> cancel(@PathVariable("productId") Long productId) {
        productFavoriteService.cancel(productId);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<FavoriteProductVO>> page(@RequestParam(name = "pageNum", defaultValue = "1") Long pageNum,
                                                      @RequestParam(name = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.success(productFavoriteService.page(pageNum, pageSize));
    }

    @GetMapping("/check/{productId:\\d+}")
    public Result<Boolean> check(@PathVariable("productId") Long productId) {
        return Result.success(productFavoriteService.check(productId));
    }

    @GetMapping("/check/batch")
    public Result<Set<Long>> checkBatch(@RequestParam(name = "productIds", required = false) List<Long> productIds) {
        return Result.success(productFavoriteService.checkBatch(productIds));
    }
}
