package com.youxuan.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.youxuan.cart.client.ProductClient;
import com.youxuan.cart.client.vo.ProductClientVO;
import com.youxuan.cart.dto.CartAddRequest;
import com.youxuan.cart.dto.CartCheckAllRequest;
import com.youxuan.cart.dto.CartCheckRequest;
import com.youxuan.cart.dto.CartUpdateRequest;
import com.youxuan.cart.entity.CartItem;
import com.youxuan.cart.mapper.CartItemMapper;
import com.youxuan.cart.service.CartService;
import com.youxuan.cart.vo.CartItemVO;
import com.youxuan.common.context.UserContext;
import com.youxuan.common.exception.BusinessException;
import com.youxuan.common.result.ErrorCode;
import com.youxuan.common.result.Result;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 购物车服务实现，所有写操作都限定在当前登录用户自己的购物车数据内。
 */
@Service
public class CartServiceImpl implements CartService {

    private static final int CHECKED = 1;
    private static final int PRODUCT_ON = 1;

    private final CartItemMapper cartItemMapper;
    private final ProductClient productClient;

    public CartServiceImpl(CartItemMapper cartItemMapper, ProductClient productClient) {
        this.cartItemMapper = cartItemMapper;
        this.productClient = productClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartItemVO add(CartAddRequest request) {
        Long userId = currentUserId();
        ProductClientVO product = getProduct(request.getProductId());

        CartItem existingItem = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, request.getProductId())
                .last("LIMIT 1"));
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            existingItem.setChecked(CHECKED);
            cartItemMapper.updateById(existingItem);
            return toVO(existingItem, product);
        }

        CartItem cartItem = new CartItem();
        cartItem.setUserId(userId);
        cartItem.setProductId(request.getProductId());
        cartItem.setQuantity(request.getQuantity());
        cartItem.setChecked(CHECKED);
        cartItem.setDeleted(0);
        cartItemMapper.insert(cartItem);
        return toVO(cartItemMapper.selectById(cartItem.getId()), product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartItemVO updateQuantity(CartUpdateRequest request) {
        CartItem cartItem = getCurrentUserCartItem(request.getCartItemId());
        cartItem.setQuantity(request.getQuantity());
        cartItemMapper.updateById(cartItem);
        return toVO(cartItem, getProduct(cartItem.getProductId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long cartItemId) {
        CartItem cartItem = getCurrentUserCartItem(cartItemId);
        cartItemMapper.deleteById(cartItem.getId());
    }

    @Override
    public List<CartItemVO> list() {
        Long userId = currentUserId();
        return cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .orderByDesc(CartItem::getUpdateTime)
                        .orderByDesc(CartItem::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartItemVO check(CartCheckRequest request) {
        CartItem cartItem = getCurrentUserCartItem(request.getCartItemId());
        cartItem.setChecked(request.getChecked());
        cartItemMapper.updateById(cartItem);
        return toVO(cartItem, getProduct(cartItem.getProductId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAll(CartCheckAllRequest request) {
        Long userId = currentUserId();
        cartItemMapper.update(null, new LambdaUpdateWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .set(CartItem::getChecked, request.getChecked()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChecked() {
        Long userId = currentUserId();
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getChecked, CHECKED));
    }

    private CartItem getCurrentUserCartItem(Long cartItemId) {
        if (cartItemId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "购物车项ID不能为空");
        }
        CartItem cartItem = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getId, cartItemId)
                .eq(CartItem::getUserId, currentUserId())
                .last("LIMIT 1"));
        if (cartItem == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "购物车项不存在");
        }
        return cartItem;
    }

    private CartItemVO toVO(CartItem cartItem) {
        ProductClientVO product = getProduct(cartItem.getProductId());
        return toVO(cartItem, product);
    }

    private CartItemVO toVO(CartItem cartItem, ProductClientVO product) {
        CartItemVO vo = new CartItemVO();
        vo.setCartItemId(cartItem.getId());
        vo.setProductId(cartItem.getProductId());
        vo.setProductName(product.getName());
        vo.setMainImage(product.getMainImage());
        vo.setPrice(product.getPrice());
        vo.setQuantity(cartItem.getQuantity());
        vo.setChecked(cartItem.getChecked());
        vo.setStock(product.getStock());
        vo.setStatus(product.getStatus());
        vo.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

        boolean offShelf = !Integer.valueOf(PRODUCT_ON).equals(product.getStatus());
        boolean stockInsufficient = product.getStock() == null || product.getStock() < cartItem.getQuantity();
        vo.setOffShelf(offShelf);
        vo.setStockInsufficient(stockInsufficient);
        if (offShelf) {
            vo.setInvalidReason("商品已下架");
        } else if (stockInsufficient) {
            vo.setInvalidReason("商品库存不足");
        }
        return vo;
    }

    private ProductClientVO getProduct(Long productId) {
        if (productId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "商品ID不能为空");
        }
        Result<ProductClientVO> result = productClient.detail(productId);
        if (result == null || !Integer.valueOf(200).equals(result.getCode()) || result.getData() == null) {
            String message = result == null ? "商品不存在" : result.getMessage();
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, message);
        }
        ProductClientVO product = result.getData();
        if (product.getPrice() == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "商品价格异常");
        }
        return product;
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return userId;
    }
}
