package com.youxuan.product.controller;

import com.youxuan.common.result.PageResult;
import com.youxuan.common.result.Result;
import com.youxuan.product.dto.ProductReviewCreateDTO;
import com.youxuan.product.service.ProductReviewService;
import com.youxuan.product.vo.ProductReviewSummaryVO;
import com.youxuan.product.vo.ProductReviewVO;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品评价接口。
 */
@RestController
@RequestMapping(value = "/{productId:\\d+}/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductReviewController {

    private final ProductReviewService productReviewService;

    public ProductReviewController(ProductReviewService productReviewService) {
        this.productReviewService = productReviewService;
    }

    @GetMapping("/page")
    public Result<PageResult<ProductReviewVO>> page(@PathVariable("productId") Long productId,
                                                    @RequestParam(name = "pageNum", defaultValue = "1") Long pageNum,
                                                    @RequestParam(name = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.success(productReviewService.page(productId, pageNum, pageSize));
    }

    @GetMapping("/summary")
    public Result<ProductReviewSummaryVO> summary(@PathVariable("productId") Long productId) {
        return Result.success(productReviewService.summary(productId));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<ProductReviewVO> create(@PathVariable("productId") Long productId,
                                          @Valid @RequestBody ProductReviewCreateDTO createDTO) {
        return Result.success(productReviewService.create(productId, createDTO));
    }
}
