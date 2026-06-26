package com.youxuan.coupon.controller;

import com.youxuan.common.result.PageResult;
import com.youxuan.common.result.Result;
import com.youxuan.coupon.dto.CouponCreateRequest;
import com.youxuan.coupon.service.CouponService;
import com.youxuan.coupon.vo.CouponVO;
import com.youxuan.coupon.vo.UserCouponVO;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 优惠券接口。
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<CouponVO> create(@Valid @RequestBody CouponCreateRequest request) {
        return Result.success(couponService.create(request));
    }

    @GetMapping("/page")
    public Result<PageResult<CouponVO>> page(@RequestParam(name = "pageNum", defaultValue = "1") Long pageNum,
                                             @RequestParam(name = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.success(couponService.page(pageNum, pageSize));
    }

    @PostMapping("/{id:\\d+}/preheat")
    public Result<Void> preheat(@PathVariable("id") Long id) {
        couponService.preheat(id);
        return Result.success();
    }

    @PostMapping("/{id:\\d+}/receive")
    public Result<Void> receive(@PathVariable("id") Long id) {
        couponService.receive(id);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<List<UserCouponVO>> myCoupons() {
        return Result.success(couponService.myCoupons());
    }

    @GetMapping("/available")
    public Result<List<CouponVO>> availableCoupons(@RequestParam("amount") BigDecimal amount) {
        return Result.success(couponService.availableCoupons(amount));
    }
}
