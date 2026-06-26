package com.youxuan.cart.service;

import com.youxuan.cart.dto.CartAddRequest;
import com.youxuan.cart.dto.CartCheckAllRequest;
import com.youxuan.cart.dto.CartCheckRequest;
import com.youxuan.cart.dto.CartUpdateRequest;
import com.youxuan.cart.vo.CartItemVO;
import java.util.List;

/**
 * 购物车服务。
 */
public interface CartService {

    CartItemVO add(CartAddRequest request);

    CartItemVO updateQuantity(CartUpdateRequest request);

    void delete(Long cartItemId);

    List<CartItemVO> list();

    CartItemVO check(CartCheckRequest request);

    void checkAll(CartCheckAllRequest request);

    void deleteChecked();
}
