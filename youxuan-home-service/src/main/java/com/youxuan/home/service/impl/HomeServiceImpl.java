package com.youxuan.home.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youxuan.common.constant.RedisKeyConstants;
import com.youxuan.common.exception.BusinessException;
import com.youxuan.common.result.ErrorCode;
import com.youxuan.common.result.Result;
import com.youxuan.home.client.ProductClient;
import com.youxuan.home.client.vo.ProductCategoryClientVO;
import com.youxuan.home.client.vo.ProductClientVO;
import com.youxuan.home.dto.BannerCreateRequest;
import com.youxuan.home.dto.BannerUpdateRequest;
import com.youxuan.home.dto.RecommendCreateRequest;
import com.youxuan.home.entity.HomeBanner;
import com.youxuan.home.entity.HomeRecommend;
import com.youxuan.home.mapper.HomeBannerMapper;
import com.youxuan.home.mapper.HomeRecommendMapper;
import com.youxuan.home.service.HomeService;
import com.youxuan.home.vo.BannerVO;
import com.youxuan.home.vo.HomeIndexVO;
import com.youxuan.home.vo.RecommendProductVO;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 首页运营服务实现，负责 Banner、推荐位和首页聚合缓存。
 */
@Service
public class HomeServiceImpl implements HomeService {

    private static final Logger log = LoggerFactory.getLogger(HomeServiceImpl.class);
    private static final Duration HOME_CACHE_TTL = Duration.ofMinutes(30);
    private static final int ENABLED = 1;
    private static final int PRODUCT_ON = 1;
    private static final int DEFAULT_HOT_LIMIT = 10;

    private final HomeBannerMapper homeBannerMapper;
    private final HomeRecommendMapper homeRecommendMapper;
    private final ProductClient productClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public HomeServiceImpl(HomeBannerMapper homeBannerMapper,
                           HomeRecommendMapper homeRecommendMapper,
                           ProductClient productClient,
                           StringRedisTemplate stringRedisTemplate,
                           ObjectMapper objectMapper) {
        this.homeBannerMapper = homeBannerMapper;
        this.homeRecommendMapper = homeRecommendMapper;
        this.productClient = productClient;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BannerVO createBanner(BannerCreateRequest request) {
        validateBannerTime(request.getStartTime(), request.getEndTime());
        HomeBanner banner = new HomeBanner();
        fillBanner(banner, request.getTitle(), request.getImageUrl(), request.getLinkType(), request.getLinkValue(),
                request.getSort(), request.getStatus(), request.getStartTime(), request.getEndTime());
        banner.setDeleted(0);
        homeBannerMapper.insert(banner);
        deleteBannerCaches();
        return BannerVO.from(homeBannerMapper.selectById(banner.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BannerVO updateBanner(Long id, BannerUpdateRequest request) {
        HomeBanner banner = getBanner(id);
        validateBannerTime(request.getStartTime(), request.getEndTime());
        fillBanner(banner, request.getTitle(), request.getImageUrl(), request.getLinkType(), request.getLinkValue(),
                request.getSort(), request.getStatus(), request.getStartTime(), request.getEndTime());
        homeBannerMapper.updateById(banner);
        deleteBannerCaches();
        return BannerVO.from(homeBannerMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBanner(Long id) {
        HomeBanner banner = getBanner(id);
        homeBannerMapper.deleteById(banner.getId());
        deleteBannerCaches();
    }

    @Override
    public List<BannerVO> listBanners() {
        return homeBannerMapper.selectList(new LambdaQueryWrapper<HomeBanner>()
                        .orderByAsc(HomeBanner::getSort)
                        .orderByDesc(HomeBanner::getId))
                .stream()
                .map(BannerVO::from)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RecommendProductVO createRecommend(RecommendCreateRequest request) {
        ProductClientVO product = getProduct(request.getProductId());
        HomeRecommend recommend = new HomeRecommend();
        recommend.setTitle(request.getTitle().trim());
        recommend.setProductId(request.getProductId());
        recommend.setSort(request.getSort() == null ? 0 : request.getSort());
        recommend.setStatus(request.getStatus() == null ? ENABLED : request.getStatus());
        recommend.setDeleted(0);
        homeRecommendMapper.insert(recommend);
        deleteRecommendCaches();
        return RecommendProductVO.from(homeRecommendMapper.selectById(recommend.getId()), product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecommend(Long id) {
        HomeRecommend recommend = getRecommend(id);
        homeRecommendMapper.deleteById(recommend.getId());
        deleteRecommendCaches();
    }

    @Override
    public HomeIndexVO index() {
        HomeIndexVO cached = readCache(RedisKeyConstants.HOME_INDEX, new TypeReference<HomeIndexVO>() {
        });
        if (cached != null) {
            log.info("首页聚合缓存命中 key={}", RedisKeyConstants.HOME_INDEX);
            return cached;
        }

        log.info("首页聚合缓存未命中 key={}", RedisKeyConstants.HOME_INDEX);
        HomeIndexVO homeIndexVO = new HomeIndexVO();
        homeIndexVO.setBanners(getActiveBanners());
        homeIndexVO.setCategories(getCategories());
        homeIndexVO.setHotProducts(getHotProducts());
        homeIndexVO.setRecommendProducts(getRecommendProducts());
        writeCache(RedisKeyConstants.HOME_INDEX, homeIndexVO);
        return homeIndexVO;
    }

    private HomeBanner getBanner(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "Banner ID不能为空");
        }
        HomeBanner banner = homeBannerMapper.selectById(id);
        if (banner == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Banner不存在");
        }
        return banner;
    }

    private HomeRecommend getRecommend(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "推荐位ID不能为空");
        }
        HomeRecommend recommend = homeRecommendMapper.selectById(id);
        if (recommend == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "推荐位不存在");
        }
        return recommend;
    }

    private void validateBannerTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "Banner结束时间不能早于开始时间");
        }
    }

    private void fillBanner(HomeBanner banner, String title, String imageUrl, String linkType, String linkValue,
                            Integer sort, Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        banner.setTitle(title.trim());
        banner.setImageUrl(imageUrl.trim());
        banner.setLinkType(linkType);
        banner.setLinkValue(linkValue);
        banner.setSort(sort == null ? 0 : sort);
        banner.setStatus(status == null ? ENABLED : status);
        banner.setStartTime(startTime);
        banner.setEndTime(endTime);
    }

    private List<BannerVO> getActiveBanners() {
        List<BannerVO> cached = readCache(RedisKeyConstants.HOME_BANNERS, new TypeReference<List<BannerVO>>() {
        });
        if (cached != null) {
            log.info("首页 Banner 缓存命中 key={}", RedisKeyConstants.HOME_BANNERS);
            return cached;
        }
        LocalDateTime now = LocalDateTime.now();
        List<BannerVO> banners = homeBannerMapper.selectList(new LambdaQueryWrapper<HomeBanner>()
                        .eq(HomeBanner::getStatus, ENABLED)
                        .and(wrapper -> wrapper.isNull(HomeBanner::getStartTime)
                                .or()
                                .le(HomeBanner::getStartTime, now))
                        .and(wrapper -> wrapper.isNull(HomeBanner::getEndTime)
                                .or()
                                .ge(HomeBanner::getEndTime, now))
                        .orderByAsc(HomeBanner::getSort)
                        .orderByDesc(HomeBanner::getId))
                .stream()
                .map(BannerVO::from)
                .toList();
        writeCache(RedisKeyConstants.HOME_BANNERS, banners);
        return banners;
    }

    private List<RecommendProductVO> getRecommendProducts() {
        List<RecommendProductVO> cached = readCache(RedisKeyConstants.HOME_RECOMMEND_PRODUCTS,
                new TypeReference<List<RecommendProductVO>>() {
                });
        if (cached != null) {
            log.info("首页推荐商品缓存命中 key={}", RedisKeyConstants.HOME_RECOMMEND_PRODUCTS);
            return cached;
        }
        List<RecommendProductVO> recommendProducts = homeRecommendMapper.selectList(new LambdaQueryWrapper<HomeRecommend>()
                        .eq(HomeRecommend::getStatus, ENABLED)
                        .orderByAsc(HomeRecommend::getSort)
                        .orderByDesc(HomeRecommend::getId))
                .stream()
                .map(this::toRecommendProductVO)
                .filter(recommendProduct -> Integer.valueOf(PRODUCT_ON).equals(recommendProduct.getStatus()))
                .toList();
        writeCache(RedisKeyConstants.HOME_RECOMMEND_PRODUCTS, recommendProducts);
        return recommendProducts;
    }

    private RecommendProductVO toRecommendProductVO(HomeRecommend recommend) {
        ProductClientVO product = getProduct(recommend.getProductId());
        return RecommendProductVO.from(recommend, product);
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
        return result.getData();
    }

    private List<ProductCategoryClientVO> getCategories() {
        Result<List<ProductCategoryClientVO>> result = productClient.categories();
        if (result == null || !Integer.valueOf(200).equals(result.getCode()) || result.getData() == null) {
            String message = result == null ? "商品分类获取失败" : result.getMessage();
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, message);
        }
        return result.getData();
    }

    private List<ProductClientVO> getHotProducts() {
        Result<List<ProductClientVO>> result = productClient.hotProducts(DEFAULT_HOT_LIMIT);
        if (result == null || !Integer.valueOf(200).equals(result.getCode()) || result.getData() == null) {
            String message = result == null ? "热门商品获取失败" : result.getMessage();
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, message);
        }
        return result.getData();
    }

    private <T> T readCache(String key, TypeReference<T> typeReference) {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, typeReference);
        } catch (JsonProcessingException exception) {
            log.warn("首页缓存反序列化失败，已删除缓存 key={}", key, exception);
            stringRedisTemplate.delete(key);
            return null;
        }
    }

    private void writeCache(String key, Object value) {
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), HOME_CACHE_TTL);
            log.info("首页缓存写入 key={}", key);
        } catch (JsonProcessingException exception) {
            log.warn("首页缓存写入失败 key={}", key, exception);
        }
    }

    private void deleteBannerCaches() {
        stringRedisTemplate.delete(List.of(RedisKeyConstants.HOME_INDEX, RedisKeyConstants.HOME_BANNERS));
        log.info("首页 Banner 相关缓存已删除");
    }

    private void deleteRecommendCaches() {
        stringRedisTemplate.delete(List.of(RedisKeyConstants.HOME_INDEX, RedisKeyConstants.HOME_RECOMMEND_PRODUCTS));
        log.info("首页推荐位相关缓存已删除");
    }
}
