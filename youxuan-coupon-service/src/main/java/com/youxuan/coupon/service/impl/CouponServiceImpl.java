package com.youxuan.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youxuan.common.constant.RedisKeyConstants;
import com.youxuan.common.context.UserContext;
import com.youxuan.common.exception.BusinessException;
import com.youxuan.common.message.CouponReceiveMessage;
import com.youxuan.common.result.ErrorCode;
import com.youxuan.common.result.PageResult;
import com.youxuan.coupon.dto.CouponCreateRequest;
import com.youxuan.coupon.dto.CouponRestoreRequest;
import com.youxuan.coupon.dto.CouponUpdateRequest;
import com.youxuan.coupon.dto.CouponUseRequest;
import com.youxuan.coupon.entity.Coupon;
import com.youxuan.coupon.entity.UserCoupon;
import com.youxuan.coupon.mapper.CouponMapper;
import com.youxuan.coupon.mapper.UserCouponMapper;
import com.youxuan.coupon.mq.CouponReceiveProducer;
import com.youxuan.coupon.service.CouponService;
import com.youxuan.coupon.vo.CouponVO;
import com.youxuan.coupon.vo.UserCouponVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 优惠券服务实现。
 */
@Service
public class CouponServiceImpl implements CouponService {

    private static final Logger log = LoggerFactory.getLogger(CouponServiceImpl.class);

    private static final int STATUS_ENABLED = 1;
    private static final int STATUS_DISABLED = 0;
    private static final String ROLE_ADMIN = "ADMIN";
    private static final int USER_COUPON_UNUSED = 0;
    private static final int USER_COUPON_USED = 1;
    private static final long LUA_SUCCESS = 0L;
    private static final long LUA_STOCK_EMPTY = 1L;
    private static final long LUA_DUPLICATE_RECEIVE = 2L;

    private static final String RECEIVE_LUA = """
            if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
                return 2
            end
            local stock = tonumber(redis.call('GET', KEYS[1]) or '-1')
            if stock <= 0 then
                return 1
            end
            redis.call('DECR', KEYS[1])
            redis.call('SADD', KEYS[2], ARGV[1])
            return 0
            """;

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final CouponReceiveProducer couponReceiveProducer;

    public CouponServiceImpl(CouponMapper couponMapper,
                             UserCouponMapper userCouponMapper,
                             StringRedisTemplate stringRedisTemplate,
                             CouponReceiveProducer couponReceiveProducer) {
        this.couponMapper = couponMapper;
        this.userCouponMapper = userCouponMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.couponReceiveProducer = couponReceiveProducer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponVO create(CouponCreateRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "结束时间必须晚于开始时间");
        }
        int availableStock = request.getAvailableStock() == null ? request.getTotalStock() : request.getAvailableStock();
        if (availableStock > request.getTotalStock()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "剩余库存不能大于总库存");
        }

        Coupon coupon = new Coupon();
        coupon.setName(request.getName().trim());
        coupon.setAmount(request.getAmount());
        coupon.setMinAmount(request.getMinAmount());
        coupon.setTotalStock(request.getTotalStock());
        coupon.setAvailableStock(availableStock);
        coupon.setStartTime(request.getStartTime());
        coupon.setEndTime(request.getEndTime());
        coupon.setStatus(request.getStatus() == null ? STATUS_ENABLED : request.getStatus());
        coupon.setDeleted(0);
        couponMapper.insert(coupon);
        return CouponVO.from(couponMapper.selectById(coupon.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponVO update(Long couponId, CouponUpdateRequest request) {
        requireAdmin();
        Coupon coupon = getCoupon(couponId);
        validateUpdateTimeRange(coupon, request);
        validateUpdateStock(coupon, request);
        validateSensitiveUpdate(coupon, request);

        Coupon update = new Coupon();
        update.setId(coupon.getId());
        if (request.getName() != null) {
            update.setName(request.getName().trim());
        }
        if (request.getAmount() != null) {
            update.setAmount(request.getAmount());
        }
        if (request.getMinAmount() != null) {
            update.setMinAmount(request.getMinAmount());
        }
        if (request.getTotalStock() != null) {
            update.setTotalStock(request.getTotalStock());
            if (request.getAvailableStock() == null && userCouponCount(coupon.getId()) == 0) {
                update.setAvailableStock(request.getTotalStock());
            }
        }
        if (request.getAvailableStock() != null) {
            update.setAvailableStock(request.getAvailableStock());
        }
        if (request.getStartTime() != null) {
            update.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            update.setEndTime(request.getEndTime());
        }
        if (request.getStatus() != null) {
            update.setStatus(request.getStatus());
        }
        couponMapper.updateById(update);
        return CouponVO.from(couponMapper.selectById(coupon.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void up(Long couponId) {
        requireAdmin();
        Coupon coupon = getCoupon(couponId);
        if (Integer.valueOf(STATUS_ENABLED).equals(coupon.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券已上线");
        }
        if (coupon.getEndTime() != null && coupon.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "已过期优惠券不能上线");
        }
        int updated = couponMapper.update(null, new LambdaUpdateWrapper<Coupon>()
                .eq(Coupon::getId, couponId)
                .eq(Coupon::getStatus, STATUS_DISABLED)
                .set(Coupon::getStatus, STATUS_ENABLED));
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券上线失败，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void down(Long couponId) {
        requireAdmin();
        Coupon coupon = getCoupon(couponId);
        if (Integer.valueOf(STATUS_DISABLED).equals(coupon.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券已下线");
        }
        int updated = couponMapper.update(null, new LambdaUpdateWrapper<Coupon>()
                .eq(Coupon::getId, couponId)
                .eq(Coupon::getStatus, STATUS_ENABLED)
                .set(Coupon::getStatus, STATUS_DISABLED));
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券下线失败，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long couponId) {
        requireAdmin();
        Coupon coupon = getCoupon(couponId);
        long receivedCount = userCouponCount(coupon.getId());
        if (receivedCount > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR,
                    "已有用户领取该优惠券，不能删除，请先下线");
        }
        couponMapper.update(null, new LambdaUpdateWrapper<Coupon>()
                .eq(Coupon::getId, coupon.getId())
                .set(Coupon::getStatus, STATUS_DISABLED));
        int deleted = couponMapper.deleteById(coupon.getId());
        if (deleted == 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券删除失败，请刷新后重试");
        }
    }

    @Override
    public PageResult<CouponVO> page(Long pageNum, Long pageSize) {
        long current = pageNum == null || pageNum < 1 ? 1L : pageNum;
        long size = pageSize == null || pageSize < 1 ? 10L : pageSize;
        LocalDateTime now = LocalDateTime.now();
        Page<Coupon> page = couponMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Coupon>()
                        .eq(Coupon::getStatus, STATUS_ENABLED)
                        .le(Coupon::getStartTime, now)
                        .ge(Coupon::getEndTime, now)
                        .gt(Coupon::getAvailableStock, 0)
                        .orderByDesc(Coupon::getId));
        List<CouponVO> records = page.getRecords().stream().map(CouponVO::from).toList();
        return PageResult.of(page.getTotal(), current, size, records);
    }

    @Override
    public PageResult<CouponVO> adminPage(Long pageNum, Long pageSize) {
        requireAdmin();
        long current = pageNum == null || pageNum < 1 ? 1L : pageNum;
        long size = pageSize == null || pageSize < 1 ? 10L : pageSize;
        Page<Coupon> page = couponMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Coupon>().orderByDesc(Coupon::getId));
        List<CouponVO> records = page.getRecords().stream().map(CouponVO::from).toList();
        return PageResult.of(page.getTotal(), current, size, records);
    }

    @Override
    public void preheat(Long couponId) {
        Coupon coupon = getValidCoupon(couponId);
        stringRedisTemplate.opsForValue().set(stockKey(couponId), String.valueOf(coupon.getAvailableStock()));
        log.info("优惠券库存预热成功 couponId={}, availableStock={}", couponId, coupon.getAvailableStock());
    }

    @Override
    public void receive(Long couponId) {
        Long userId = currentUserId();
        getValidCoupon(couponId);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(RECEIVE_LUA, Long.class);
        Long result = stringRedisTemplate.execute(script,
                List.of(stockKey(couponId), userKey(couponId)),
                String.valueOf(userId));
        if (result == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券领取失败");
        }
        if (result == LUA_STOCK_EMPTY) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券库存不足或未预热");
        }
        if (result == LUA_DUPLICATE_RECEIVE) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "不能重复领取优惠券");
        }
        if (result != LUA_SUCCESS) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券领取失败");
        }
        couponReceiveProducer.send(new CouponReceiveMessage(userId, couponId, LocalDateTime.now()));
    }

    @Override
    public List<UserCouponVO> myCoupons() {
        Long userId = currentUserId();
        return userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .orderByDesc(UserCoupon::getReceiveTime)
                        .orderByDesc(UserCoupon::getId))
                .stream()
                .map(userCoupon -> UserCouponVO.from(userCoupon, couponMapper.selectById(userCoupon.getCouponId())))
                .toList();
    }

    @Override
    public List<CouponVO> availableCoupons(BigDecimal amount) {
        Long userId = currentUserId();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "订单金额不能小于0");
        }
        LocalDateTime now = LocalDateTime.now();
        return userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getStatus, USER_COUPON_UNUSED)
                        .orderByDesc(UserCoupon::getReceiveTime)
                        .orderByDesc(UserCoupon::getId))
                .stream()
                .map(userCoupon -> couponMapper.selectById(userCoupon.getCouponId()))
                .filter(coupon -> isCouponAvailableForAmount(coupon, amount, now))
                .map(CouponVO::from)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void use(CouponUseRequest request) {
        Long userId = currentUserId();
        Coupon coupon = getValidCoupon(request.getCouponId());
        validateCouponStockModel(coupon);
        if (coupon.getMinAmount().compareTo(request.getOrderAmount()) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "订单金额未满足优惠券使用门槛");
        }
        UserCoupon userCoupon = userCouponMapper.selectOne(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, request.getCouponId())
                .last("LIMIT 1"));
        if (userCoupon == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户优惠券不存在");
        }
        if (!Integer.valueOf(USER_COUPON_UNUSED).equals(userCoupon.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券不可用");
        }
        int updated = userCouponMapper.update(null, new LambdaUpdateWrapper<UserCoupon>()
                .eq(UserCoupon::getId, userCoupon.getId())
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, USER_COUPON_UNUSED)
                .set(UserCoupon::getStatus, USER_COUPON_USED)
                .set(UserCoupon::getUseTime, LocalDateTime.now())
                .set(UserCoupon::getOrderId, request.getOrderId()));
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券核销失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(CouponRestoreRequest request) {
        Long userId = currentUserId();
        int updated = userCouponMapper.update(null, new LambdaUpdateWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, request.getCouponId())
                .eq(UserCoupon::getOrderId, request.getOrderId())
                .eq(UserCoupon::getStatus, USER_COUPON_USED)
                .set(UserCoupon::getStatus, USER_COUPON_UNUSED)
                .set(UserCoupon::getUseTime, null)
                .set(UserCoupon::getOrderId, null));
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券恢复失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeReceiveMessage(CouponReceiveMessage message) {
        if (message == null || message.getUserId() == null || message.getCouponId() == null) {
            log.warn("收到无效优惠券领取消息 message={}", message);
            return;
        }
        Long userId = message.getUserId();
        Long couponId = message.getCouponId();
        UserCoupon existing = userCouponMapper.selectOne(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId)
                .last("LIMIT 1"));
        if (existing != null) {
            log.warn("用户优惠券已存在，忽略重复消息 userId={}, couponId={}", userId, couponId);
            return;
        }

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(USER_COUPON_UNUSED);
        userCoupon.setReceiveTime(message.getReceiveTime() == null ? LocalDateTime.now() : message.getReceiveTime());
        try {
            userCouponMapper.insert(userCoupon);
        } catch (DuplicateKeyException exception) {
            log.warn("用户优惠券唯一索引冲突，忽略重复写入 userId={}, couponId={}", userId, couponId);
            return;
        }

        int updated = couponMapper.update(null, new LambdaUpdateWrapper<Coupon>()
                .eq(Coupon::getId, couponId)
                .gt(Coupon::getAvailableStock, 0)
                .setSql("available_stock = available_stock - 1"));
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券数据库库存不足");
        }
        log.info("优惠券领取落库成功 userId={}, couponId={}", userId, couponId);
    }

    private void validateUpdateTimeRange(Coupon coupon, CouponUpdateRequest request) {
        LocalDateTime startTime = request.getStartTime() == null ? coupon.getStartTime() : request.getStartTime();
        LocalDateTime endTime = request.getEndTime() == null ? coupon.getEndTime() : request.getEndTime();
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "结束时间必须晚于开始时间");
        }
    }

    private void validateUpdateStock(Coupon coupon, CouponUpdateRequest request) {
        int totalStock = request.getTotalStock() == null ? coupon.getTotalStock() : request.getTotalStock();
        int availableStock = request.getAvailableStock() == null ? coupon.getAvailableStock() : request.getAvailableStock();
        long receivedCount = userCouponCount(coupon.getId());
        if (availableStock > totalStock) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "剩余库存不能大于总库存");
        }
        if (totalStock < receivedCount) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "总库存不能小于已领取数量");
        }
    }

    private void validateSensitiveUpdate(Coupon coupon, CouponUpdateRequest request) {
        long receivedCount = userCouponCount(coupon.getId());
        if (receivedCount == 0) {
            return;
        }
        boolean usedOrOrderReferenced = usedCouponCount(coupon.getId()) > 0 || orderReferencedCouponCount(coupon.getId()) > 0;
        if (usedOrOrderReferenced && hasUsedCouponSensitiveChange(coupon, request)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR,
                    "已有核销或订单引用，仅允许修改优惠券名称和上下线状态");
        }
        if (hasReceivedCouponSensitiveChange(coupon, request)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR,
                    "已有用户领取，仅允许修改优惠券名称、结束时间和上下线状态");
        }
    }

    private boolean hasUsedCouponSensitiveChange(Coupon coupon, CouponUpdateRequest request) {
        return isChanged(coupon.getAmount(), request.getAmount())
                || isChanged(coupon.getMinAmount(), request.getMinAmount())
                || isChanged(coupon.getTotalStock(), request.getTotalStock())
                || isChanged(coupon.getAvailableStock(), request.getAvailableStock())
                || isChanged(coupon.getStartTime(), request.getStartTime())
                || isChanged(coupon.getEndTime(), request.getEndTime());
    }

    private boolean hasReceivedCouponSensitiveChange(Coupon coupon, CouponUpdateRequest request) {
        return isChanged(coupon.getAmount(), request.getAmount())
                || isChanged(coupon.getMinAmount(), request.getMinAmount())
                || isChanged(coupon.getTotalStock(), request.getTotalStock())
                || isChanged(coupon.getAvailableStock(), request.getAvailableStock())
                || isChanged(coupon.getStartTime(), request.getStartTime());
    }

    private boolean isChanged(Object oldValue, Object newValue) {
        if (oldValue instanceof BigDecimal oldDecimal && newValue instanceof BigDecimal newDecimal) {
            return oldDecimal.compareTo(newDecimal) != 0;
        }
        return newValue != null && !Objects.equals(oldValue, newValue);
    }

    private Coupon getCoupon(Long couponId) {
        if (couponId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券ID不能为空");
        }
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在");
        }
        return coupon;
    }

    private long userCouponCount(Long couponId) {
        return userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getCouponId, couponId));
    }

    private long usedCouponCount(Long couponId) {
        return userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getCouponId, couponId)
                .eq(UserCoupon::getStatus, USER_COUPON_USED));
    }

    private long orderReferencedCouponCount(Long couponId) {
        return userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getCouponId, couponId)
                .isNotNull(UserCoupon::getOrderId));
    }

    private Coupon getValidCoupon(Long couponId) {
        if (couponId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券ID不能为空");
        }
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在");
        }
        if (!Integer.valueOf(STATUS_ENABLED).equals(coupon.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券未启用");
        }
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartTime().isAfter(now) || coupon.getEndTime().isBefore(now)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券不在有效期内");
        }
        return coupon;
    }

    private void requireAdmin() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        if (!ROLE_ADMIN.equals(UserContext.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无优惠券管理权限");
        }
    }

    private boolean isCouponAvailableForAmount(Coupon coupon, BigDecimal amount, LocalDateTime now) {
        return coupon != null
                && Integer.valueOf(STATUS_ENABLED).equals(coupon.getStatus())
                && coupon.getMinAmount() != null
                && coupon.getMinAmount().compareTo(amount) <= 0
                && coupon.getStartTime() != null
                && coupon.getEndTime() != null
                && !coupon.getStartTime().isAfter(now)
                && !coupon.getEndTime().isBefore(now)
                && hasUsableStockModel(coupon);
    }

    private void validateCouponStockModel(Coupon coupon) {
        if (!hasUsableStockModel(coupon)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券库存异常");
        }
    }

    private boolean hasUsableStockModel(Coupon coupon) {
        return coupon.getTotalStock() != null
                && coupon.getTotalStock() > 0
                && coupon.getAvailableStock() != null
                && coupon.getAvailableStock() >= 0;
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return userId;
    }

    private String stockKey(Long couponId) {
        return RedisKeyConstants.COUPON_STOCK + couponId;
    }

    private String userKey(Long couponId) {
        return RedisKeyConstants.COUPON_USER + couponId;
    }
}
