package com.youxuan.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youxuan.common.constant.RedisKeyConstants;
import com.youxuan.common.context.UserContext;
import com.youxuan.common.exception.BusinessException;
import com.youxuan.common.message.OrderTimeoutMessage;
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
import com.youxuan.order.dto.OrderShipRequest;
import com.youxuan.order.entity.MallOrder;
import com.youxuan.order.entity.MallOrderItem;
import com.youxuan.order.mapper.MallOrderItemMapper;
import com.youxuan.order.mapper.MallOrderMapper;
import com.youxuan.order.mq.OrderTimeoutProducer;
import com.youxuan.order.service.OrderService;
import com.youxuan.order.support.OrderNoGenerator;
import com.youxuan.order.vo.OrderDetailVO;
import com.youxuan.order.vo.OrderItemVO;
import com.youxuan.order.vo.OrderPageVO;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
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
 * 订单服务实现，覆盖下单、支付、取消、发货和确认收货主链路。
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private static final String SOURCE_CART = "CART";
    private static final String SOURCE_BUY_NOW = "BUY_NOW";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final int ORDER_STATUS_WAIT_PAY = 0;
    private static final int ORDER_STATUS_WAIT_DELIVERY = 1;
    private static final int ORDER_STATUS_WAIT_RECEIVE = 2;
    private static final int ORDER_STATUS_COMPLETED = 3;
    private static final int ORDER_STATUS_CANCELED = 4;
    private static final int PRODUCT_ON = 1;
    private static final int SUCCESS_CODE = 200;
    private static final String SCOPE_ALL = "ALL";
    private static final String SCOPE_CATEGORY = "CATEGORY";
    private static final Duration SUBMIT_KEY_TTL = Duration.ofSeconds(10);

    private final MallOrderMapper mallOrderMapper;
    private final MallOrderItemMapper mallOrderItemMapper;
    private final ProductClient productClient;
    private final UserAddressClient userAddressClient;
    private final CouponClient couponClient;
    private final CartClient cartClient;
    private final OrderNoGenerator orderNoGenerator;
    private final StringRedisTemplate stringRedisTemplate;
    private final OrderTimeoutProducer orderTimeoutProducer;

    public OrderServiceImpl(MallOrderMapper mallOrderMapper,
                            MallOrderItemMapper mallOrderItemMapper,
                            ProductClient productClient,
                            UserAddressClient userAddressClient,
                            CouponClient couponClient,
                            CartClient cartClient,
                            OrderNoGenerator orderNoGenerator,
                            StringRedisTemplate stringRedisTemplate,
                            OrderTimeoutProducer orderTimeoutProducer) {
        this.mallOrderMapper = mallOrderMapper;
        this.mallOrderItemMapper = mallOrderItemMapper;
        this.productClient = productClient;
        this.userAddressClient = userAddressClient;
        this.couponClient = couponClient;
        this.cartClient = cartClient;
        this.orderNoGenerator = orderNoGenerator;
        this.stringRedisTemplate = stringRedisTemplate;
        this.orderTimeoutProducer = orderTimeoutProducer;
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
            CouponClientVO selectedCoupon = resolveSelectedCoupon(request.getCouponId(), totalAmount, snapshots, userId, role);
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

            orderTimeoutProducer.sendAfterCommit(order.getId(), order.getOrderNo());
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
        return buildDetailVO(order);
    }

    private OrderDetailVO buildDetailVO(MallOrder order) {
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

    @Override
    public PageResult<OrderPageVO> adminOrders(Long pageNum, Long pageSize, Integer status) {
        if (!ROLE_ADMIN.equals(currentRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无订单管理权限");
        }
        long current = pageNum == null || pageNum < 1 ? 1L : pageNum;
        long size = pageSize == null || pageSize < 1 ? 10L : pageSize;
        Page<MallOrder> page = mallOrderMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<MallOrder>()
                        .eq(status != null, MallOrder::getStatus, status)
                        .orderByDesc(MallOrder::getCreateTime)
                        .orderByDesc(MallOrder::getId));
        List<OrderPageVO> records = page.getRecords().stream().map(OrderPageVO::from).toList();
        return PageResult.of(page.getTotal(), current, size, records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO pay(Long id) {
        Long userId = currentUserId();
        MallOrder order = getCurrentUserOrder(id, userId);
        if (Integer.valueOf(ORDER_STATUS_WAIT_DELIVERY).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "订单已支付");
        }
        if (Integer.valueOf(ORDER_STATUS_CANCELED).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "已取消订单不能支付");
        }
        if (!Integer.valueOf(ORDER_STATUS_WAIT_PAY).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "订单状态不允许支付");
        }
        int updated = mallOrderMapper.update(null, new LambdaUpdateWrapper<MallOrder>()
                .eq(MallOrder::getId, order.getId())
                .eq(MallOrder::getUserId, userId)
                .eq(MallOrder::getStatus, ORDER_STATUS_WAIT_PAY)
                .set(MallOrder::getStatus, ORDER_STATUS_WAIT_DELIVERY)
                .set(MallOrder::getPayTime, LocalDateTime.now()));
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "订单支付失败，请刷新后重试");
        }
        return detail(order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO cancel(Long id) {
        Long userId = currentUserId();
        MallOrder order = getCurrentUserOrder(id, userId);
        cancelWaitPayOrder(order, userId, currentRole(), true);
        return detail(order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO ship(Long id, OrderShipRequest request) {
        if (!ROLE_ADMIN.equals(currentRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无发货权限");
        }
        MallOrder order = getOrderById(id);
        validateCanShip(order);
        LocalDateTime now = LocalDateTime.now();
        int updated = mallOrderMapper.update(null, new LambdaUpdateWrapper<MallOrder>()
                .eq(MallOrder::getId, order.getId())
                .eq(MallOrder::getStatus, ORDER_STATUS_WAIT_DELIVERY)
                .set(MallOrder::getStatus, ORDER_STATUS_WAIT_RECEIVE)
                .set(MallOrder::getDeliveryCompany, request.getDeliveryCompany())
                .set(MallOrder::getTrackingNo, request.getTrackingNo())
                .set(MallOrder::getDeliveryTime, now));
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "订单发货失败，请刷新后重试");
        }
        MallOrder updatedOrder = getOrderById(order.getId());
        return buildDetailVO(updatedOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO receive(Long id) {
        Long userId = currentUserId();
        MallOrder order = getCurrentUserOrder(id, userId);
        validateCanReceive(order);
        int updated = mallOrderMapper.update(null, new LambdaUpdateWrapper<MallOrder>()
                .eq(MallOrder::getId, order.getId())
                .eq(MallOrder::getUserId, userId)
                .eq(MallOrder::getStatus, ORDER_STATUS_WAIT_RECEIVE)
                .set(MallOrder::getStatus, ORDER_STATUS_COMPLETED)
                .set(MallOrder::getReceiveTime, LocalDateTime.now()));
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "确认收货失败，请刷新后重试");
        }
        return detail(order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void timeoutCancel(OrderTimeoutMessage message) {
        if (message == null || message.getOrderId() == null) {
            log.warn("收到无效订单超时消息 message={}", message);
            return;
        }
        MallOrder order = mallOrderMapper.selectById(message.getOrderId());
        if (order == null) {
            log.warn("订单超时消息对应订单不存在 orderId={}", message.getOrderId());
            return;
        }
        if (message.getOrderNo() != null && !message.getOrderNo().equals(order.getOrderNo())) {
            log.warn("订单超时消息订单号不匹配 messageOrderNo={}, dbOrderNo={}",
                    message.getOrderNo(), order.getOrderNo());
            return;
        }
        if (!Integer.valueOf(ORDER_STATUS_WAIT_PAY).equals(order.getStatus())) {
            log.info("订单已非待支付状态，忽略超时取消 orderId={}, status={}", order.getId(), order.getStatus());
            return;
        }
        cancelWaitPayOrder(order, order.getUserId(), "USER", false);
        log.info("订单超时自动取消成功 orderId={}, orderNo={}", order.getId(), order.getOrderNo());
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
        return new OrderItemSnapshot(product.getId(), product.getCategoryId(), product.getName(), product.getMainImage(),
                product.getPrice(), itemRequest.getQuantity(), totalAmount);
    }

    private CouponClientVO resolveSelectedCoupon(Long couponId, BigDecimal totalAmount, List<OrderItemSnapshot> snapshots,
                                                 Long userId, String role) {
        if (couponId == null) {
            return null;
        }
        List<CouponClientVO> availableCoupons = unwrap(couponClient.available(totalAmount, userId, role),
                "查询可用优惠券失败");
        CouponClientVO selected = availableCoupons.stream()
                .filter(coupon -> couponId.equals(coupon.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券不可用"));
        validateCouponAppliesToSnapshots(selected, snapshots);
        return selected;
    }

    private void validateCouponAppliesToSnapshots(CouponClientVO coupon, List<OrderItemSnapshot> snapshots) {
        String scopeType = coupon.getScopeType() == null ? SCOPE_ALL : coupon.getScopeType();
        if (SCOPE_ALL.equals(scopeType)) {
            return;
        }
        if (SCOPE_CATEGORY.equals(scopeType)) {
            Long categoryId = coupon.getCategoryId();
            if (categoryId == null) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券适用分类异常");
            }
            boolean allMatched = snapshots.stream().allMatch(snapshot -> categoryId.equals(snapshot.categoryId()));
            if (!allMatched) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券不适用于当前商品");
            }
            return;
        }
        throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券适用范围异常");
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

    private void cancelWaitPayOrder(MallOrder order, Long userId, String role, boolean manual) {
        if (!Integer.valueOf(ORDER_STATUS_WAIT_PAY).equals(order.getStatus())) {
            if (manual && Integer.valueOf(ORDER_STATUS_WAIT_DELIVERY).equals(order.getStatus())) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "已支付订单不能取消");
            }
            if (manual && Integer.valueOf(ORDER_STATUS_CANCELED).equals(order.getStatus())) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "订单已取消");
            }
            if (manual) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "订单状态不允许取消");
            }
            return;
        }
        int updated = mallOrderMapper.update(null, new LambdaUpdateWrapper<MallOrder>()
                .eq(MallOrder::getId, order.getId())
                .eq(MallOrder::getStatus, ORDER_STATUS_WAIT_PAY)
                .set(MallOrder::getStatus, ORDER_STATUS_CANCELED)
                .set(MallOrder::getCancelTime, LocalDateTime.now()));
        if (updated == 0) {
            if (manual) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "订单取消失败，请刷新后重试");
            }
            log.info("订单超时取消时状态已变化 orderId={}", order.getId());
            return;
        }

        List<MallOrderItem> items = mallOrderItemMapper.selectList(new LambdaQueryWrapper<MallOrderItem>()
                .eq(MallOrderItem::getOrderId, order.getId()));
        for (MallOrderItem item : items) {
            unwrapVoid(productClient.restoreStock(new StockChangeClientRequest(item.getProductId(), item.getQuantity())),
                    "恢复商品库存失败");
        }
        if (order.getCouponId() != null) {
            unwrapVoid(couponClient.restore(new CouponRestoreClientRequest(order.getCouponId(), order.getId()),
                    userId, role), "恢复优惠券失败");
        }
    }

    private MallOrder getCurrentUserOrder(Long id, Long userId) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "订单ID不能为空");
        }
        MallOrder order = mallOrderMapper.selectOne(new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getId, id)
                .eq(MallOrder::getUserId, userId)
                .last("LIMIT 1"));
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    private MallOrder getOrderById(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "订单ID不能为空");
        }
        MallOrder order = mallOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    private void validateCanShip(MallOrder order) {
        if (Integer.valueOf(ORDER_STATUS_WAIT_DELIVERY).equals(order.getStatus())) {
            return;
        }
        if (Integer.valueOf(ORDER_STATUS_WAIT_PAY).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "待支付订单不能发货");
        }
        if (Integer.valueOf(ORDER_STATUS_CANCELED).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "已取消订单不能发货");
        }
        if (Integer.valueOf(ORDER_STATUS_COMPLETED).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "已完成订单不能发货");
        }
        throw new BusinessException(ErrorCode.BUSINESS_ERROR, "订单状态不允许发货");
    }

    private void validateCanReceive(MallOrder order) {
        if (Integer.valueOf(ORDER_STATUS_WAIT_RECEIVE).equals(order.getStatus())) {
            return;
        }
        if (Integer.valueOf(ORDER_STATUS_WAIT_PAY).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "待支付订单不能确认收货");
        }
        if (Integer.valueOf(ORDER_STATUS_WAIT_DELIVERY).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "待发货订单不能确认收货");
        }
        if (Integer.valueOf(ORDER_STATUS_CANCELED).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "已取消订单不能确认收货");
        }
        if (Integer.valueOf(ORDER_STATUS_COMPLETED).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "订单已完成");
        }
        throw new BusinessException(ErrorCode.BUSINESS_ERROR, "订单状态不允许确认收货");
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

    private record OrderItemSnapshot(Long productId, Long categoryId, String productName, String productImage,
                                     BigDecimal price, Integer quantity, BigDecimal totalAmount) {
    }

    private record DeductedStock(Long productId, Integer quantity) {
    }
}
