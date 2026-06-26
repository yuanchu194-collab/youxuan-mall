package com.youxuan.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youxuan.common.exception.BusinessException;
import com.youxuan.common.result.ErrorCode;
import com.youxuan.product.dto.ProductCategoryCreateDTO;
import com.youxuan.product.entity.ProductCategory;
import com.youxuan.product.mapper.ProductCategoryMapper;
import com.youxuan.product.service.ProductCategoryService;
import com.youxuan.product.vo.ProductCategoryVO;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品分类服务实现。
 */
@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private static final int STATUS_ENABLED = 1;

    private final ProductCategoryMapper productCategoryMapper;

    public ProductCategoryServiceImpl(ProductCategoryMapper productCategoryMapper) {
        this.productCategoryMapper = productCategoryMapper;
    }

    /**
     * 创建分类时校验同一父分类下名称唯一，避免前端展示重复分类。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductCategoryVO create(ProductCategoryCreateDTO createDTO) {
        Long parentId = createDTO.getParentId() == null ? 0L : createDTO.getParentId();
        if (parentId > 0) {
            ProductCategory parent = productCategoryMapper.selectById(parentId);
            if (parent == null || !Integer.valueOf(STATUS_ENABLED).equals(parent.getStatus())) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "父分类不存在或已禁用");
            }
        }
        ProductCategory exists = productCategoryMapper.selectOne(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getParentId, parentId)
                .eq(ProductCategory::getName, createDTO.getName().trim())
                .last("LIMIT 1"));
        if (exists != null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "同级分类名称已存在");
        }

        ProductCategory category = new ProductCategory();
        category.setName(createDTO.getName().trim());
        category.setParentId(parentId);
        category.setSort(createDTO.getSort() == null ? 0 : createDTO.getSort());
        category.setStatus(createDTO.getStatus() == null ? STATUS_ENABLED : createDTO.getStatus());
        category.setDeleted(0);
        productCategoryMapper.insert(category);
        return ProductCategoryVO.from(productCategoryMapper.selectById(category.getId()));
    }

    @Override
    public List<ProductCategoryVO> list() {
        return productCategoryMapper.selectList(new LambdaQueryWrapper<ProductCategory>()
                        .orderByAsc(ProductCategory::getSort)
                        .orderByDesc(ProductCategory::getId))
                .stream()
                .map(ProductCategoryVO::from)
                .toList();
    }
}
