package com.youxuan.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youxuan.common.constant.RedisKeyConstants;
import com.youxuan.common.context.UserContext;
import com.youxuan.common.exception.BusinessException;
import com.youxuan.common.result.ErrorCode;
import com.youxuan.common.result.PageResult;
import com.youxuan.common.result.Result;
import com.youxuan.order.client.CartClient;
import com.youxuan.order.client.CouponClient;
import com.youxuan.order.client.ProductClient;
import com.youxuan.order.client.UserAddressClient;
import com.youxuan.order.client.dto.CouponRestoreClientRequest;
import com.youxuan.order.client.dto.CouponUseClientRequest;
import com.youxuan.order.client.dto.StockChangeClientRequest;
import com.youxuan.order.client.vo.CouponClientVO;
import com.youxuan.order.client.vo.ProductClientVO;
import com.youxuan.order.client.vo.UserAddressClientVO;
import com.youxuan.order.dto.OrderCreateItemRequest;
import com.youxuan.order.dto.OrderCreateRequest;
import com.youxuan.order.entity.MallOrder;
import com.youxuan.order.entity.MallOrderItem;
import com.youxuan.order.mapper.MallOrderItemMapper;
import com.youxuan.order.mapper.MallOrderMapper;
import com.youxuan.order.service.OrderService;
import com.youxuan.order.support.OrderNoGenerator;
import com.youxuan.order.vo.OrderDetailVO;
import com.youxuan.order.vo.OrderItemVO;
import com.youxuan.order.vo.OrderPageVO;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 订单服务实现。当前阶段只创建待支付订单，不实现支付、取消、发货和确认收货。
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private static final String SOURCE_CART = "CART";
    private static final String SOURCE_BUY_NOW = "BUY_NOW";
    private static final int ORDER_STATUS_WAIT_PAY = 0;
    private static final int PRODUCT_ON = 1;
    private static final int SUCCESS_CODE = 200;
    private static final Duration SUBMIT_KEY_TTL = Duration.ofSeconds(10);

    private final MallOrderMapper mallOrderMapper;
    private final MallOrderItemMapper mallOrderItemMapper;
    private final ProductClient productClient;
    private final UserAddressClient userAddressClient;
    private final CouponClient couponClient;
    private final CartClient cartClient;
    private final OrderNoGenerator orderNoGenerator;
    private final StringRedisTemplate stringRedisTemplate;

    public OrderServiceImpl(MallOrderMapper mallOrderMapper,
                            MallOrderItemMapper mallOrderItemMapper,
                            ProductClient productClient,
                            UserAddressClient userAddressClient,
                            CouponClient couponClient,
                            CartClient cartClient,
                            OrderNoGenerator orderNoGenerator,
                            StringRedisTemplate stringRedisTemplate) {
        this.mallOrderMapper = mallOrderMapper;
        this.mallOrderItemMapper = mallOrderItemMapper;
        this.productClient = productClient;
        this.userAddressClient = userAddressClient;
        this.couponClient = couponClient;
        this.cartClient = cartClient;
        this.orderNoGenerator = orderNoGenerator;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO create(OrderCreateRequest request) {
        Long userId = currentUserId();
        String role = currentRole();
        validateSource(request.getSource());
        validateDuplicateProducts(request.getItems());
        acquireSubmitLock(userId, request.getItems());

        UserAddressClientVO address = null;
        MallOrder order = null;
        List<OrderItemSnapshot> snapshots = List.of();
        List<DeductedStock> deductedStocks = new ArrayList<>();
        boolean couponUsed = false;

        try {
            address = unwrap(userAddressClient.detail(request.getAddressId(), userId, role), "收货地址不存在");
            snapshots = request.getItems().stream().map(this::buildSnapshot).toList();
            BigDecimal totalAmount = snapshots.stream()
                    .map(OrderItemSnapshot::totalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            CouponClientVO selectedCoupon = resolveSelectedCoupon(request.getCouponId(), totalAmount, userId, role);
            BigDecimal discountAmount = selectedCoupon == null ? BigDecimal.ZERO : selectedCoupon.getAmount();
            BigDecimal payAmount = totalAmount.subtract(discountAmount);
            if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
                payAmount = BigDecimal.ZERO;
            }

            order = buildOrder(userId, request.getCouponId(), address, totalAmount, discountAmount, payAmount);
            mallOrderMapper.insert(order);

            for (OrderItemSnapshot snapshot : snapshots) {
                unwrapVoid(productClient.deductStock(
                        new StockChangeClientRequest(snapshot.productId(), snapshot.quantity())), "扣减商品库存失败");
                deductedStocks.add(new DeductedStock(snapshot.productId(), snapshot.quantity()));
                mallOrderItemMapper.insert(toOrderItem(order.getId(), snapshot));
            }

            if (request.getCouponId() != null) {
                unwrapVoid(couponClient.use(
                        new CouponUseClientRequest(request.getCouponId(), order.getId(), totalAmount), userId, role),
                        "优惠券核销失败");
                couponUsed = true;
            }

            registerCartCleanup(request.getSource(), userId, role);
            return detail(order.getId());
        } catch (RuntimeException exception) {
            if (couponUsed && order != null && request.getCouponId() != null) {
                restoreCouponQuietly(request.getCouponId(), order.getId(), userId, role);
            }
            restoreStocksQuietly(deductedStocks);
            throw exception;
        }
    }

    @Override
    public OrderDetailVO detail(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "订单ID不能为空");
        }
        Long userId = currentUserId();
        MallOrder order = mallOrderMapper.selectOne(new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getId, id)
                .eq(MallOrder::getUserId, userId)
                .last("LIMIT 1"));
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        List<OrderItemVO> items = mallOrderItemMapper.selectList(new LambdaQueryWrapper<MallOrderItem>()
                        .eq(MallOrderItem::getOrderId, order.getId())
                        .orderByAsc(MallOrderItem::getId))
                .stream()
                .map(OrderItemVO::from)
                .toList();
        return OrderDetailVO.from(order, items);
    }

    @Override
    public PageResult<OrderPageVO> myOrders(Long pageNum, Long pageSize) {
        Long userId = currentUserId();
        long current = pageNum == null || pageNum < 1 ? 1L : pageNum;
        long size = pageSize == null || pageSize < 1 ? 10L : pageSize;
        Page<MallOrder> page = mallOrderMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<MallOrder>()
                        .eq(MallOrder::getUserId, userId)
                        .orderByDesc(MallOrder::getCreateTime)
                        .orderByDesc(MallOrder::getId));
        List<OrderPageVO> records = page.getRecords().stream().map(OrderPageVO::from).toList();
        return PageResult.of(page.getTotal(), current, size, records);
    }

    private OrderItemSnapshot buildSnapshot(OrderCreateItemRequest itemRequest) {
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
        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
        return new OrderItemSnapshot(product.getId(), product.getName(), product.getMainImage(),
                product.getPrice(), itemRequest.getQuantity(), totalAmount);
    }

    private CouponClientVO resolveSelectedCoupon(Long couponId, BigDecimal totalAmount, Long userId, String role) {
        if (couponId == null) {
            return null;
        }
        List<CouponClientVO> availableCoupons = unwrap(couponClient.available(totalAmount, userId, role),
                "查询可用优惠券失败");
        return availableCoupons.stream()
                .filter(coupon -> couponId.equals(coupon.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券不可用"));
    }

    private MallOrder buildOrder(Long userId, Long couponId, UserAddressClientVO address,
                                 BigDecimal totalAmount, BigDecimal discountAmount, BigDecimal payAmount) {
        MallOrder order = new MallOrder();
        order.setOrderNo(orderNoGenerator.next());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(payAmount);
        order.setStatus(ORDER_STATUS_WAIT_PAY);
        order.setCouponId(couponId);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(fullAddress(address));
        order.setDeleted(0);
        return order;
    }

    private MallOrderItem toOrderItem(Long orderId, OrderItemSnapshot snapshot) {
        MallOrderItem item = new MallOrderItem();
        item.setOrderId(orderId);
        item.setProductId(snapshot.productId());
        item.setProductName(snapshot.productName());
        item.setProductImage(snapshot.productImage());
        item.setPrice(snapshot.price());
        item.setQuantity(snapshot.quantity());
        item.setTotalAmount(snapshot.totalAmount());
        return item;
    }

    private void acquireSubmitLock(Long userId, List<OrderCreateItemRequest> items) {
        Long firstProductId = items.stream()
                .map(OrderCreateItemRequest::getProductId)
                .min(Long::compareTo)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "订单商品不能为空"));
        String key = RedisKeyConstants.ORDER_SUBMIT + userId + ":" + firstProductId;
        Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", SUBMIT_KEY_TTL);
        if (!Boolean.TRUE.equals(acquired)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "请勿重复提交订单");
        }
    }

    private void registerCartCleanup(String source, Long userId, String role) {
        if (!SOURCE_CART.equals(source) || !TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    Result<Void> result = cartClient.deleteChecked(userId, role);
                    if (result == null || !Integer.valueOf(SUCCESS_CODE).equals(result.getCode())) {
                        log.warn("订单创建后清理购物车失败 userId={}, message={}",
                                userId, result == null ? "无响应" : result.getMessage());
                    }
                } catch (RuntimeException exception) {
                    log.warn("订单创建后清理购物车异常 userId={}", userId, exception);
                }
            }
        });
    }

    private void restoreStocksQuietly(List<DeductedStock> deductedStocks) {
        for (DeductedStock deductedStock : deductedStocks) {
            try {
                productClient.restoreStock(
                        new StockChangeClientRequest(deductedStock.productId(), deductedStock.quantity()));
            } catch (RuntimeException exception) {
                log.error("订单创建失败后恢复库存异常 productId={}, quantity={}",
                        deductedStock.productId(), deductedStock.quantity(), exception);
            }
        }
    }

    private void restoreCouponQuietly(Long couponId, Long orderId, Long userId, String role) {
        try {
            couponClient.restore(new CouponRestoreClientRequest(couponId, orderId), userId, role);
        } catch (RuntimeException exception) {
            log.error("订单创建失败后恢复优惠券异常 couponId={}, orderId={}", couponId, orderId, exception);
        }
    }

    private void validateDuplicateProducts(List<OrderCreateItemRequest> items) {
        Set<Long> productIds = new HashSet<>();
        for (OrderCreateItemRequest item : items) {
            if (!productIds.add(item.getProductId())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "订单商品不能重复");
            }
        }
    }

    private void validateSource(String source) {
        if (!SOURCE_CART.equals(source) && !SOURCE_BUY_NOW.equals(source)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "订单来源只能是CART或BUY_NOW");
        }
    }

    private String fullAddress(UserAddressClientVO address) {
        return safe(address.getProvince()) + safe(address.getCity())
                + safe(address.getDistrict()) + safe(address.getDetailAddress());
    }

    private String safe(String value) {
        return value == null ? "" : value;
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

    private void unwrapVoid(Result<?> result, String fallbackMessage) {
        if (result == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, fallbackMessage);
        }
        if (!Integer.valueOf(SUCCESS_CODE).equals(result.getCode())) {
            throw new BusinessException(result.getCode(), result.getMessage());
        }
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

    private record OrderItemSnapshot(Long productId, String productName, String productImage,
                                     BigDecimal price, Integer quantity, BigDecimal totalAmount) {
    }

    private record DeductedStock(Long productId, Integer quantity) {
    }
}
