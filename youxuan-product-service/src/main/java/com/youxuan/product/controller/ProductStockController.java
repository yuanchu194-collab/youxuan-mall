package com.youxuan.product.controller;

import com.youxuan.common.result.Result;
import com.youxuan.product.dto.StockChangeDTO;
import com.youxuan.product.service.ProductService;
import com.youxuan.product.vo.ProductVO;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品库存接口。
 */
@RestController
@RequestMapping(value = "/stock", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductStockController {

    private final ProductService productService;

    public ProductStockController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(value = "/deduct", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<ProductVO> deduct(@Valid @RequestBody StockChangeDTO stockChangeDTO) {
        return Result.success(productService.deductStock(stockChangeDTO));
    }

    @PostMapping(value = "/restore", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<ProductVO> restore(@Valid @RequestBody StockChangeDTO stockChangeDTO) {
        return Result.success(productService.restoreStock(stockChangeDTO));
    }
}
