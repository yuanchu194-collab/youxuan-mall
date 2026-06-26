package com.youxuan.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youxuan.coupon.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户优惠券数据访问接口。
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {
}
