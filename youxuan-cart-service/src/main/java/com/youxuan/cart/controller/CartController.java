package com.youxuan.cart.controller;

import com.youxuan.cart.dto.CartAddRequest;
import com.youxuan.cart.dto.CartCheckAllRequest;
import com.youxuan.cart.dto.CartCheckRequest;
import com.youxuan.cart.dto.CartUpdateRequest;
import com.youxuan.cart.service.CartService;
import com.youxuan.cart.vo.CartItemVO;
import com.youxuan.common.result.Result;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 购物车接口。
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<CartItemVO> add(@Valid @RequestBody CartAddRequest request) {
        return Result.success(cartService.add(request));
    }

    @PutMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<CartItemVO> update(@Valid @RequestBody CartUpdateRequest request) {
        return Result.success(cartService.updateQuantity(request));
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        cartService.delete(id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<CartItemVO>> list() {
        return Result.success(cartService.list());
    }

    @PutMapping(value = "/check", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<CartItemVO> check(@Valid @RequestBody CartCheckRequest request) {
        return Result.success(cartService.check(request));
    }

    @PutMapping(value = "/checkAll", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<Void> checkAll(@Valid @RequestBody CartCheckAllRequest request) {
        cartService.checkAll(request);
        return Result.success();
    }

    @DeleteMapping("/checked")
    public Result<Void> deleteChecked() {
        cartService.deleteChecked();
        return Result.success();
    }
}
