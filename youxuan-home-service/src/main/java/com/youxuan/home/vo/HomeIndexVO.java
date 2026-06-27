package com.youxuan.home.vo;

import com.youxuan.home.client.vo.ProductCategoryClientVO;
import com.youxuan.home.client.vo.ProductClientVO;
import java.util.List;

/**
 * 首页聚合响应。
 */
public class HomeIndexVO {

    private List<BannerVO> banners;
    private List<ProductCategoryClientVO> categories;
    private List<ProductClientVO> hotProducts;
    private List<RecommendProductVO> recommendProducts;

    public List<BannerVO> getBanners() {
        return banners;
    }

    public void setBanners(List<BannerVO> banners) {
        this.banners = banners;
    }

    public List<ProductCategoryClientVO> getCategories() {
        return categories;
    }

    public void setCategories(List<ProductCategoryClientVO> categories) {
        this.categories = categories;
    }

    public List<ProductClientVO> getHotProducts() {
        return hotProducts;
    }

    public void setHotProducts(List<ProductClientVO> hotProducts) {
        this.hotProducts = hotProducts;
    }

    public List<RecommendProductVO> getRecommendProducts() {
        return recommendProducts;
    }

    public void setRecommendProducts(List<RecommendProductVO> recommendProducts) {
        this.recommendProducts = recommendProducts;
    }
}
