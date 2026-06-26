package com.youxuan.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youxuan.common.constant.RedisKeyConstants;
import com.youxuan.common.exception.BusinessException;
import com.youxuan.common.result.ErrorCode;
import com.youxuan.product.vo.ProductVO;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 商品缓存服务，集中处理 Redis key、空值缓存和缓存日志。
 */
@Service
public class ProductCacheService {

    private static final Logger log = LoggerFactory.getLogger(ProductCacheService.class);
    private static final String NULL_VALUE = "null";
    private static final Duration PRODUCT_DETAIL_TTL = Duration.ofMinutes(30);
    private static final Duration EMPTY_PRODUCT_TTL = Duration.ofMinutes(2);
    private static final Duration HOT_PRODUCTS_TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public ProductCacheService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public String getProductDetailCacheKey(Long productId) {
        return RedisKeyConstants.PRODUCT_DETAIL + productId;
    }

    public ProductVO getProductDetail(Long productId) {
        String key = getProductDetailCacheKey(productId);
        String value = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(value)) {
            log.info("商品详情缓存未命中 productId={}", productId);
            return null;
        }
        if (NULL_VALUE.equals(value)) {
            log.info("商品详情空值缓存命中 productId={}", productId);
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        try {
            log.info("商品详情缓存命中 productId={}", productId);
            return objectMapper.readValue(value, ProductVO.class);
        } catch (JsonProcessingException exception) {
            log.warn("商品详情缓存反序列化失败，已删除缓存 productId={}", productId, exception);
            stringRedisTemplate.delete(key);
            return null;
        }
    }

    public void writeProductDetail(Long productId, ProductVO productVO) {
        try {
            String key = getProductDetailCacheKey(productId);
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(productVO), PRODUCT_DETAIL_TTL);
        } catch (JsonProcessingException exception) {
            log.warn("商品详情缓存写入失败 productId={}", productId, exception);
        }
    }

    public void writeEmptyProductDetail(Long productId) {
        stringRedisTemplate.opsForValue().set(getProductDetailCacheKey(productId), NULL_VALUE, EMPTY_PRODUCT_TTL);
    }

    public void deleteProductDetailCache(Long productId) {
        if (productId == null) {
            return;
        }
        stringRedisTemplate.delete(getProductDetailCacheKey(productId));
        log.info("商品详情缓存删除 productId={}", productId);
    }

    public String getHotProductsCacheKey(Integer limit) {
        int safeLimit = limit == null ? 10 : limit;
        if (safeLimit == 10) {
            return RedisKeyConstants.HOME_HOT_PRODUCTS;
        }
        return RedisKeyConstants.HOME_HOT_PRODUCTS + ":" + safeLimit;
    }

    public List<ProductVO> getHotProducts(Integer limit) {
        String key = getHotProductsCacheKey(limit);
        String value = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(value)) {
            log.info("热门商品缓存未命中 key={}", key);
            return null;
        }
        try {
            log.info("热门商品缓存命中 key={}", key);
            return objectMapper.readValue(value, new TypeReference<List<ProductVO>>() {
            });
        } catch (JsonProcessingException exception) {
            log.warn("热门商品缓存反序列化失败，已删除缓存 key={}", key, exception);
            stringRedisTemplate.delete(key);
            return null;
        }
    }

    public void writeHotProducts(Integer limit, List<ProductVO> products) {
        try {
            String key = getHotProductsCacheKey(limit);
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(products), HOT_PRODUCTS_TTL);
        } catch (JsonProcessingException exception) {
            log.warn("热门商品缓存写入失败 limit={}", limit, exception);
        }
    }

    public void deleteHotProductsCache() {
        stringRedisTemplate.delete(RedisKeyConstants.HOME_HOT_PRODUCTS);
        Set<String> keys = stringRedisTemplate.keys(RedisKeyConstants.HOME_HOT_PRODUCTS + ":*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
        log.info("热门商品缓存删除");
    }
}
