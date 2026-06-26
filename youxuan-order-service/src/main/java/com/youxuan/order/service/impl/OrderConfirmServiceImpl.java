package com.youxuan.order.service.impl;

import com.youxuan.common.context.UserContext;
import com.youxuan.common.exception.BusinessException;
import com.youxuan.common.result.ErrorCode;
import com.youxuan.common.result.Result;
import com.youxuan.order.client.CouponClient;
import com.youxuan.order.client.ProductClient;
import com.youxuan.order.client.UserAddressClient;
import com.youxuan.order.client.vo.CouponClientVO;
import com.youxuan.order.client.vo.ProductClientVO;
import com.youxuan.order.client.vo.UserAddressClientVO;
import com.youxuan.order.dto.OrderConfirmItemRequest;
import com.youxuan.order.dto.OrderConfirmRequest;
import com.youxuan.order.service.OrderConfirmService;
import com.youxuan.order.vo.OrderAddressVO;
import com.youxuan.order.vo.OrderConfirmItemVO;
import com.youxuan.order.vo.OrderConfirmVO;
import com.youxuan.order.vo.OrderCouponVO;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 订单确认页服务实现，本阶段只计算价格，不创建订单、不扣库存、不核销优惠券。
 */
@Service
public class OrderConfirmServiceImpl implements OrderConfirmService {

    private static final String SOURCE_CART = "CART";
    private static final String SOURCE_BUY_NOW = "BUY_NOW";
    private static final int PRODUCT_ON = 1;
    private static final int SUCCESS_CODE = 200;

    private final ProductClient productClient;
    private final UserAddressClient userAddressClient;
    private final CouponClient couponClient;

    public OrderConfirmServiceImpl(ProductClient productClient,
                                   UserAddressClient userAddressClient,
                                   CouponClient couponClient) {
        this.productClient = productClient;
        this.userAddressClient = userAddressClient;
        this.couponClient = couponClient;
    }

    @Override
    public OrderConfirmVO confirm(OrderConfirmRequest request) {
        Long userId = currentUserId();
        String role = currentRole();
        validateSource(request.getSource());

        UserAddressClientVO address = unwrap(userAddressClient.detail(request.getAddressId(), userId, role),
                "收货地址不存在");

        List<OrderConfirmItemVO> items = request.getItems().stream()
                .map(this::buildItem)
                .toList();
        BigDecimal totalAmount = items.stream()
                .map(OrderConfirmItemVO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OrderCouponVO> availableCoupons = unwrap(couponClient.available(totalAmount, userId, role),
                "查询可用优惠券失败")
                .stream()
                .map(OrderCouponVO::from)
                .toList();

        OrderCouponVO selectedCoupon = resolveSelectedCoupon(request.getCouponId(), availableCoupons);
        BigDecimal discountAmount = selectedCoupon == null ? BigDecimal.ZERO : selectedCoupon.getAmount();
        BigDecimal payAmount = totalAmount.subtract(discountAmount);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }

        OrderConfirmVO vo = new OrderConfirmVO();
        vo.setAddress(OrderAddressVO.from(address));
        vo.setItems(items);
        vo.setAvailableCoupons(availableCoupons);
        vo.setSelectedCoupon(selectedCoupon);
        vo.setTotalAmount(totalAmount);
        vo.setDiscountAmount(discountAmount);
        vo.setPayAmount(payAmount);
        return vo;
    }

    private OrderConfirmItemVO buildItem(OrderConfirmItemRequest itemRequest) {
        ProductClientVO product = unwrap(productClient.detail(itemRequest.getProductId()), "商品不存在");
        if (!Integer.valueOf(PRODUCT_ON).equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "商品已下架");
        }
        if (product.getStock() == null || product.getStock() < itemRequest.getQuantity()) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "商品库存不足");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "商品价格异常");
        }
        return OrderConfirmItemVO.from(product, itemRequest.getQuantity());
    }

    private OrderCouponVO resolveSelectedCoupon(Long couponId, List<OrderCouponVO> availableCoupons) {
        if (couponId == null) {
            return null;
        }
        return availableCoupons.stream()
                .filter(coupon -> couponId.equals(coupon.getCouponId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券不可用"));
    }

    private void validateSource(String source) {
        if (!SOURCE_CART.equals(source) && !SOURCE_BUY_NOW.equals(source)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "订单来源只能是CART或BUY_NOW");
        }
    }

    private <T> T unwrap(Result<T> result, String fallbackMessage) {
        if (result == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, fallbackMessage);
        }
        if (!Integer.valueOf(SUCCESS_CODE).equals(result.getCode())) {
            throw new BusinessException(result.getCode(), result.getMessage());
        }
        if (result.getData() == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, fallbackMessage);
        }
        return result.getData();
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return userId;
    }

    private String currentRole() {
        return UserContext.getRole() == null ? "USER" : UserContext.getRole();
    }
}
