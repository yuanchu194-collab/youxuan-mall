package com.youxuan.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youxuan.cart.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 购物车数据访问接口。
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {

    @Select("""
            SELECT id, user_id, product_id, quantity, checked, create_time, update_time, deleted
            FROM cart_item
            WHERE user_id = #{userId}
              AND product_id = #{productId}
            LIMIT 1
            """)
    CartItem selectByUserIdAndProductIdIncludingDeleted(@Param("userId") Long userId,
                                                        @Param("productId") Long productId);

    @Update("""
            UPDATE cart_item
            SET quantity = #{quantity},
                checked = #{checked},
                deleted = 0,
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    int restoreById(@Param("id") Long id,
                    @Param("quantity") Integer quantity,
                    @Param("checked") Integer checked);
}
