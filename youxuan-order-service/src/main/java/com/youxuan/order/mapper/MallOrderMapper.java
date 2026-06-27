package com.youxuan.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youxuan.order.entity.MallOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单主表数据访问。
 */
@Mapper
public interface MallOrderMapper extends BaseMapper<MallOrder> {
}
