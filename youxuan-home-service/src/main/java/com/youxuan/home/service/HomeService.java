package com.youxuan.home.service;

import com.youxuan.home.dto.BannerCreateRequest;
import com.youxuan.home.dto.BannerUpdateRequest;
import com.youxuan.home.dto.RecommendCreateRequest;
import com.youxuan.home.vo.BannerVO;
import com.youxuan.home.vo.HomeIndexVO;
import com.youxuan.home.vo.RecommendProductVO;
import java.util.List;

/**
 * 首页运营服务。
 */
public interface HomeService {

    BannerVO createBanner(BannerCreateRequest request);

    BannerVO updateBanner(Long id, BannerUpdateRequest request);

    void deleteBanner(Long id);

    List<BannerVO> listBanners();

    RecommendProductVO createRecommend(RecommendCreateRequest request);

    void deleteRecommend(Long id);

    HomeIndexVO index();
}
