package com.youxuan.product.controller;

import com.youxuan.common.result.Result;
import com.youxuan.product.dto.ProductCategoryCreateDTO;
import com.youxuan.product.service.ProductCategoryService;
import com.youxuan.product.vo.ProductCategoryVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品分类接口。
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @PostMapping(value = "/category", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<ProductCategoryVO> create(@Valid @RequestBody ProductCategoryCreateDTO createDTO) {
        return Result.success(productCategoryService.create(createDTO));
    }

    @GetMapping("/category/list")
    public Result<List<ProductCategoryVO>> list() {
        return Result.success(productCategoryService.list());
    }
}
