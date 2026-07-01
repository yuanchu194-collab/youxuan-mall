package com.youxuan.product.controller;

import com.youxuan.common.result.PageResult;
import com.youxuan.common.result.Result;
import com.youxuan.product.dto.ProductCreateDTO;
import com.youxuan.product.dto.ProductUpdateDTO;
import com.youxuan.product.service.ProductService;
import com.youxuan.product.vo.ProductVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品接口。
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<ProductVO> create(@Valid @RequestBody ProductCreateDTO createDTO) {
        return Result.success(productService.create(createDTO));
    }

    @PutMapping(value = "/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<ProductVO> update(@PathVariable("id") Long id, @Valid @RequestBody ProductUpdateDTO updateDTO) {
        return Result.success(productService.update(id, updateDTO));
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        productService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id:\\d+}")
    public Result<ProductVO> detail(@PathVariable("id") Long id) {
        return Result.success(productService.detail(id));
    }

    @GetMapping("/page")
    public Result<PageResult<ProductVO>> page(@RequestParam(name = "pageNum", defaultValue = "1") Long pageNum,
                                              @RequestParam(name = "pageSize", defaultValue = "10") Long pageSize,
                                              @RequestParam(name = "name", required = false) String name,
                                              @RequestParam(name = "categoryId", required = false) Long categoryId,
                                              @RequestParam(name = "status", required = false) Integer status,
                                              @RequestParam(name = "couponId", required = false) Long couponId) {
        return Result.success(productService.page(pageNum, pageSize, name, categoryId, status, couponId));
    }

    @GetMapping("/home/hot")
    public Result<List<ProductVO>> hotProducts(@RequestParam(name = "limit", defaultValue = "10") Integer limit) {
        return Result.success(productService.hotProducts(limit));
    }

    @PutMapping("/{id:\\d+}/up")
    public Result<Void> up(@PathVariable("id") Long id) {
        productService.up(id);
        return Result.success();
    }

    @PutMapping("/{id:\\d+}/down")
    public Result<Void> down(@PathVariable("id") Long id) {
        productService.down(id);
        return Result.success();
    }
}
