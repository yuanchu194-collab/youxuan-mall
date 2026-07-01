package com.youxuan.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youxuan.common.exception.BusinessException;
import com.youxuan.common.message.ProductSyncMessage;
import com.youxuan.common.result.ErrorCode;
import com.youxuan.common.result.PageResult;
import com.youxuan.common.result.Result;
import com.youxuan.product.client.CouponClient;
import com.youxuan.product.client.vo.CouponScopeClientVO;
import com.youxuan.product.dto.ProductCreateDTO;
import com.youxuan.product.dto.ProductUpdateDTO;
import com.youxuan.product.dto.StockChangeDTO;
import com.youxuan.product.entity.Product;
import com.youxuan.product.entity.ProductCategory;
import com.youxuan.product.entity.ProductStock;
import com.youxuan.product.mapper.ProductCategoryMapper;
import com.youxuan.product.mapper.ProductMapper;
import com.youxuan.product.mapper.ProductStockMapper;
import com.youxuan.product.mq.ProductSyncProducer;
import com.youxuan.product.service.ProductCacheService;
import com.youxuan.product.service.ProductService;
import com.youxuan.product.vo.ProductVO;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 商品服务实现。
 */
@Service
public class ProductServiceImpl implements ProductService {

    private static final int STATUS_OFF = 0;
    private static final int STATUS_ON = 1;
    private static final int SUCCESS_CODE = 200;
    private static final String SCOPE_ALL = "ALL";
    private static final String SCOPE_CATEGORY = "CATEGORY";

    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductStockMapper productStockMapper;
    private final ProductCacheService productCacheService;
    private final ProductSyncProducer productSyncProducer;
    private final CouponClient couponClient;

    public ProductServiceImpl(ProductMapper productMapper,
                              ProductCategoryMapper productCategoryMapper,
                              ProductStockMapper productStockMapper,
                              ProductCacheService productCacheService,
                              ProductSyncProducer productSyncProducer,
                              CouponClient couponClient) {
        this.productMapper = productMapper;
        this.productCategoryMapper = productCategoryMapper;
        this.productStockMapper = productStockMapper;
        this.productCacheService = productCacheService;
        this.productSyncProducer = productSyncProducer;
        this.couponClient = couponClient;
    }

    /**
     * 新增商品和初始化库存必须在同一个事务内完成，避免出现有商品无库存的脏数据。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO create(ProductCreateDTO createDTO) {
        ProductCategory category = getEnabledCategory(createDTO.getCategoryId());

        Product product = new Product();
        fillProduct(product, createDTO.getCategoryId(), createDTO.getName(), createDTO.getSubtitle(),
                createDTO.getMainImage(), createDTO.getDetail(), createDTO.getPrice(), createDTO.getOriginalPrice(),
                createDTO.getSales(), createDTO.getStatus());
        product.setDeleted(0);
        productMapper.insert(product);

        ProductStock stock = new ProductStock();
        stock.setProductId(product.getId());
        stock.setStock(createDTO.getStock());
        stock.setLockedStock(0);
        productStockMapper.insert(stock);
        ProductVO productVO = ProductVO.from(productMapper.selectById(product.getId()), category, getStock(product.getId()));
        deleteProductCaches(product.getId(), true);
        productSyncProducer.sendAfterCommit(product.getId(), ProductSyncMessage.SAVE_OR_UPDATE);
        return productVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO update(Long id, ProductUpdateDTO updateDTO) {
        getProduct(id);
        ProductCategory category = getEnabledCategory(updateDTO.getCategoryId());

        Product product = new Product();
        product.setId(id);
        fillProduct(product, updateDTO.getCategoryId(), updateDTO.getName(), updateDTO.getSubtitle(),
                updateDTO.getMainImage(), updateDTO.getDetail(), updateDTO.getPrice(), updateDTO.getOriginalPrice(),
                updateDTO.getSales(), updateDTO.getStatus());
        productMapper.updateById(product);
        ProductVO productVO = ProductVO.from(productMapper.selectById(id), category, getStock(id));
        deleteProductCaches(id, true);
        productSyncProducer.sendAfterCommit(id, ProductSyncMessage.SAVE_OR_UPDATE);
        return productVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getProduct(id);
        productMapper.deleteById(id);
        productStockMapper.delete(new LambdaQueryWrapper<ProductStock>().eq(ProductStock::getProductId, id));
        deleteProductCaches(id, true);
        productSyncProducer.sendAfterCommit(id, ProductSyncMessage.DELETE);
    }

    @Override
    public ProductVO detail(Long id) {
        validateProductId(id);
        ProductVO cachedProduct = productCacheService.getProductDetail(id);
        if (cachedProduct != null) {
            return cachedProduct;
        }

        Product product = productMapper.selectById(id);
        if (product == null) {
            // 空值缓存用于拦截不存在商品的重复查询，避免缓存穿透。
            productCacheService.writeEmptyProductDetail(id);
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        ProductVO productVO = toVO(product);
        productCacheService.writeProductDetail(id, productVO);
        return productVO;
    }

    @Override
    public PageResult<ProductVO> page(Long pageNum, Long pageSize, String name, Long categoryId, Integer status, Long couponId) {
        long current = pageNum == null || pageNum < 1 ? 1L : pageNum;
        long size = pageSize == null || pageSize < 1 ? 10L : pageSize;
        CouponScopeClientVO couponScope = couponId == null ? null : getCouponScope(couponId);
        Long effectiveCategoryId = resolveCategoryId(categoryId, couponScope);
        Integer effectiveStatus = couponScope == null ? status : Integer.valueOf(STATUS_ON);

        Page<Product> page = productMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Product>()
                        .like(StringUtils.hasText(name), Product::getName, name)
                        .eq(effectiveCategoryId != null, Product::getCategoryId, effectiveCategoryId)
                        .eq(effectiveStatus != null, Product::getStatus, effectiveStatus)
                        .orderByDesc(Product::getId));
        List<ProductVO> records = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(page.getTotal(), current, size, records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void up(Long id) {
        updateStatus(id, STATUS_ON);
        deleteProductCaches(id, true);
        productSyncProducer.sendAfterCommit(id, ProductSyncMessage.UP);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void down(Long id) {
        updateStatus(id, STATUS_OFF);
        deleteProductCaches(id, true);
        productSyncProducer.sendAfterCommit(id, ProductSyncMessage.DOWN);
    }

    /**
     * 扣减库存使用带 stock >= quantity 条件的原子更新，库存不足时不会更新任何行。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO deductStock(StockChangeDTO stockChangeDTO) {
        Product product = getProduct(stockChangeDTO.getProductId());
        int quantity = stockChangeDTO.getQuantity();
        int updated = productStockMapper.update(null, new LambdaUpdateWrapper<ProductStock>()
                .eq(ProductStock::getProductId, product.getId())
                .ge(ProductStock::getStock, quantity)
                .setSql("stock = stock - " + quantity)
                .setSql("locked_stock = locked_stock + " + quantity));
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "商品库存不足");
        }
        deleteProductCaches(product.getId(), true);
        return toVO(productMapper.selectById(product.getId()));
    }

    /**
     * 恢复库存用于后续订单取消，把锁定库存释放回可用库存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO restoreStock(StockChangeDTO stockChangeDTO) {
        Product product = getProduct(stockChangeDTO.getProductId());
        int quantity = stockChangeDTO.getQuantity();
        int updated = productStockMapper.update(null, new LambdaUpdateWrapper<ProductStock>()
                .eq(ProductStock::getProductId, product.getId())
                .ge(ProductStock::getLockedStock, quantity)
                .setSql("stock = stock + " + quantity)
                .setSql("locked_stock = locked_stock - " + quantity));
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "可恢复库存不足");
        }
        deleteProductCaches(product.getId(), true);
        return toVO(productMapper.selectById(product.getId()));
    }

    @Override
    public List<ProductVO> hotProducts(Integer limit) {
        int safeLimit = limit == null || limit < 1 ? 10 : Math.min(limit, 50);
        List<ProductVO> cachedProducts = productCacheService.getHotProducts(safeLimit);
        if (cachedProducts != null) {
            return cachedProducts;
        }

        List<ProductVO> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, STATUS_ON)
                        .orderByDesc(Product::getSales)
                        .orderByDesc(Product::getId)
                        .last("LIMIT " + safeLimit))
                .stream()
                .map(this::toVO)
                .toList();
        productCacheService.writeHotProducts(safeLimit, products);
        return products;
    }

    private void updateStatus(Long id, int status) {
        Product product = getProduct(id);
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, product.getId())
                .set(Product::getStatus, status));
    }

    private Product getProduct(Long id) {
        validateProductId(id);
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        return product;
    }

    private void validateProductId(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "商品ID不能为空");
        }
    }

    private ProductCategory getEnabledCategory(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "分类ID不能为空");
        }
        ProductCategory category = productCategoryMapper.selectById(categoryId);
        if (category == null || !Integer.valueOf(STATUS_ON).equals(category.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "商品分类不存在或已禁用");
        }
        return category;
    }

    private ProductStock getStock(Long productId) {
        return productStockMapper.selectOne(new LambdaQueryWrapper<ProductStock>()
                .eq(ProductStock::getProductId, productId)
                .last("LIMIT 1"));
    }

    private CouponScopeClientVO getCouponScope(Long couponId) {
        try {
            Result<CouponScopeClientVO> result = couponClient.scope(couponId);
            if (result == null) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券查询失败");
            }
            if (!Integer.valueOf(SUCCESS_CODE).equals(result.getCode())) {
                throw new BusinessException(result.getCode(), result.getMessage());
            }
            if (result.getData() == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在");
            }
            return result.getData();
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券查询失败");
        }
    }

    private Long resolveCategoryId(Long requestCategoryId, CouponScopeClientVO couponScope) {
        if (couponScope == null) {
            return requestCategoryId;
        }
        String scopeType = couponScope.getScopeType() == null ? SCOPE_ALL : couponScope.getScopeType();
        if (SCOPE_ALL.equals(scopeType)) {
            return requestCategoryId;
        }
        if (SCOPE_CATEGORY.equals(scopeType)) {
            Long couponCategoryId = couponScope.getCategoryId();
            if (couponCategoryId == null) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券适用分类异常");
            }
            if (requestCategoryId != null && !requestCategoryId.equals(couponCategoryId)) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "该优惠券不适用于当前分类");
            }
            return couponCategoryId;
        }
        throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券适用范围异常");
    }

    private ProductVO toVO(Product product) {
        ProductCategory category = productCategoryMapper.selectById(product.getCategoryId());
        ProductStock stock = getStock(product.getId());
        return ProductVO.from(product, category, stock);
    }

    private void deleteProductCaches(Long productId, boolean includeHotProducts) {
        productCacheService.deleteProductDetailCache(productId);
        if (includeHotProducts) {
            productCacheService.deleteHomeProductCaches();
        }
    }

    private void fillProduct(Product product, Long categoryId, String name, String subtitle, String mainImage,
                             String detail, java.math.BigDecimal price, java.math.BigDecimal originalPrice,
                             Integer sales, Integer status) {
        product.setCategoryId(categoryId);
        product.setName(name.trim());
        product.setSubtitle(subtitle);
        product.setMainImage(mainImage);
        product.setDetail(detail);
        product.setPrice(price);
        product.setOriginalPrice(originalPrice);
        product.setSales(sales == null ? 0 : sales);
        product.setStatus(status == null ? STATUS_ON : status);
    }
}
