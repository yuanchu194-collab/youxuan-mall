package com.youxuan.coupon.service;

import com.youxuan.common.message.CouponReceiveMessage;
import com.youxuan.common.result.PageResult;
import com.youxuan.coupon.dto.CouponCreateRequest;
import com.youxuan.coupon.dto.CouponRestoreRequest;
import com.youxuan.coupon.dto.CouponUseRequest;
import com.youxuan.coupon.vo.CouponVO;
import com.youxuan.coupon.vo.UserCouponVO;
import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券服务。
 */
public interface CouponService {

    CouponVO create(CouponCreateRequest request);

    PageResult<CouponVO> page(Long pageNum, Long pageSize);

    void preheat(Long couponId);

    void receive(Long couponId);

    List<UserCouponVO> myCoupons();

    List<CouponVO> availableCoupons(BigDecimal amount);

    void use(CouponUseRequest request);

    void restore(CouponRestoreRequest request);

    void consumeReceiveMessage(CouponReceiveMessage message);
}
