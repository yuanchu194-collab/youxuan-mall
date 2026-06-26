package com.youxuan.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youxuan.coupon.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券数据访问接口。
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {
}
