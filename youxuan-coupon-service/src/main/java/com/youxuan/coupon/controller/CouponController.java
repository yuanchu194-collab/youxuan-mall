package com.youxuan.coupon.controller;

import com.youxuan.common.result.PageResult;
import com.youxuan.common.result.Result;
import com.youxuan.coupon.dto.CouponCreateRequest;
import com.youxuan.coupon.dto.CouponRestoreRequest;
import com.youxuan.coupon.dto.CouponUpdateRequest;
import com.youxuan.coupon.dto.CouponUseRequest;
import com.youxuan.coupon.service.CouponService;
import com.youxuan.coupon.vo.CouponScopeVO;
import com.youxuan.coupon.vo.CouponVO;
import com.youxuan.coupon.vo.UserCouponVO;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PutMapping(value = "/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<CouponVO> update(@PathVariable("id") Long id,
                                   @Valid @RequestBody CouponUpdateRequest request) {
        return Result.success(couponService.update(id, request));
    }

    @PutMapping("/{id:\\d+}/up")
    public Result<Void> up(@PathVariable("id") Long id) {
        couponService.up(id);
        return Result.success();
    }

    @PutMapping("/{id:\\d+}/down")
    public Result<Void> down(@PathVariable("id") Long id) {
        couponService.down(id);
        return Result.success();
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        couponService.delete(id);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<CouponVO>> page(@RequestParam(name = "pageNum", defaultValue = "1") Long pageNum,
                                             @RequestParam(name = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.success(couponService.page(pageNum, pageSize));
    }

    @GetMapping("/admin/page")
    public Result<PageResult<CouponVO>> adminPage(@RequestParam(name = "pageNum", defaultValue = "1") Long pageNum,
                                                  @RequestParam(name = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.success(couponService.adminPage(pageNum, pageSize));
    }

    @GetMapping("/{id:\\d+}/scope")
    public Result<CouponScopeVO> scope(@PathVariable("id") Long id) {
        return Result.success(couponService.scope(id));
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

    @PostMapping(value = "/use", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<Void> use(@Valid @RequestBody CouponUseRequest request) {
        couponService.use(request);
        return Result.success();
    }

    @PostMapping(value = "/restore", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<Void> restore(@Valid @RequestBody CouponRestoreRequest request) {
        couponService.restore(request);
        return Result.success();
    }
}
