package com.youxuan.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youxuan.product.entity.ProductFavorite;
import com.youxuan.product.vo.FavoriteProductVO;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 商品收藏数据访问接口。
 */
@Mapper
public interface ProductFavoriteMapper extends BaseMapper<ProductFavorite> {

    @Select("""
            SELECT id, user_id, product_id, create_time, update_time, deleted
            FROM product_favorite
            WHERE user_id = #{userId}
              AND product_id = #{productId}
            LIMIT 1
            """)
    ProductFavorite selectByUserIdAndProductIdIncludingDeleted(@Param("userId") Long userId,
                                                               @Param("productId") Long productId);

    @Update("""
            UPDATE product_favorite
            SET deleted = 0,
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    int restoreById(@Param("id") Long id);

    @Select("""
            <script>
            SELECT product_id
            FROM product_favorite
            WHERE user_id = #{userId}
              AND deleted = 0
              AND product_id IN
              <foreach collection="productIds" item="productId" open="(" separator="," close=")">
                #{productId}
              </foreach>
            </script>
            """)
    Set<Long> selectCollectedProductIds(@Param("userId") Long userId,
                                        @Param("productIds") List<Long> productIds);

    @Select("""
            SELECT p.id AS product_id,
                   p.name,
                   p.price,
                   p.main_image,
                   COALESCE(ps.stock, 0) AS stock,
                   p.status,
                   pc.name AS category_name,
                   f.create_time AS collected_time
            FROM product_favorite f
            INNER JOIN product p ON p.id = f.product_id AND p.deleted = 0
            LEFT JOIN product_stock ps ON ps.product_id = p.id
            LEFT JOIN product_category pc ON pc.id = p.category_id AND pc.deleted = 0
            WHERE f.user_id = #{userId}
              AND f.deleted = 0
            ORDER BY f.create_time DESC, f.id DESC
            """)
    Page<FavoriteProductVO> selectFavoritePage(Page<FavoriteProductVO> page, @Param("userId") Long userId);
}
