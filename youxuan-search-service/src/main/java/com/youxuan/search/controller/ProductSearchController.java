package com.youxuan.search.controller;

import com.youxuan.common.result.PageResult;
import com.youxuan.common.result.Result;
import com.youxuan.search.dto.ProductSearchRequest;
import com.youxuan.search.service.ProductSearchService;
import com.youxuan.search.vo.ProductSearchVO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品搜索接口。
 */
@RestController
@RequestMapping(value = "/product", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    public ProductSearchController(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @PostMapping("/importAll")
    public Result<Integer> importAll() {
        return Result.success(productSearchService.importAll());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<PageResult<ProductSearchVO>> search(@RequestBody(required = false) ProductSearchRequest request) {
        return Result.success(productSearchService.search(request));
    }
}
