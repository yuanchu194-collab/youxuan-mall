package com.youxuan.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youxuan.coupon.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 优惠券数据访问接口。
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    @Select("""
            SELECT id, name, amount, min_amount, total_stock, available_stock, start_time, end_time,
                   status, scope_type, category_id, create_time, update_time, deleted
            FROM coupon
            WHERE id = #{id}
            LIMIT 1
            """)
    Coupon selectByIdIncludingDeleted(@Param("id") Long id);
}
