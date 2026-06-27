package com.youxuan.home.controller;

import com.youxuan.common.result.Result;
import com.youxuan.home.dto.BannerCreateRequest;
import com.youxuan.home.dto.BannerUpdateRequest;
import com.youxuan.home.dto.RecommendCreateRequest;
import com.youxuan.home.service.HomeService;
import com.youxuan.home.vo.BannerVO;
import com.youxuan.home.vo.HomeIndexVO;
import com.youxuan.home.vo.RecommendProductVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页运营接口。
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @PostMapping(value = "/banner", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<BannerVO> createBanner(@Valid @RequestBody BannerCreateRequest request) {
        return Result.success(homeService.createBanner(request));
    }

    @PutMapping(value = "/banner/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<BannerVO> updateBanner(@PathVariable("id") Long id,
                                         @Valid @RequestBody BannerUpdateRequest request) {
        return Result.success(homeService.updateBanner(id, request));
    }

    @DeleteMapping("/banner/{id:\\d+}")
    public Result<Void> deleteBanner(@PathVariable("id") Long id) {
        homeService.deleteBanner(id);
        return Result.success();
    }

    @GetMapping("/banner/list")
    public Result<List<BannerVO>> listBanners() {
        return Result.success(homeService.listBanners());
    }

    @PostMapping(value = "/recommend", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<RecommendProductVO> createRecommend(@Valid @RequestBody RecommendCreateRequest request) {
        return Result.success(homeService.createRecommend(request));
    }

    @DeleteMapping("/recommend/{id:\\d+}")
    public Result<Void> deleteRecommend(@PathVariable("id") Long id) {
        homeService.deleteRecommend(id);
        return Result.success();
    }

    @GetMapping("/index")
    public Result<HomeIndexVO> index() {
        return Result.success(homeService.index());
    }
}
