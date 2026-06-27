package com.youxuan.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youxuan.home.entity.HomeRecommend;
import org.apache.ibatis.annotations.Mapper;

/**
 * 首页推荐位数据访问接口。
 */
@Mapper
public interface HomeRecommendMapper extends BaseMapper<HomeRecommend> {
}
