package com.youxuan.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youxuan.order.entity.MallOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细数据访问。
 */
@Mapper
public interface MallOrderItemMapper extends BaseMapper<MallOrderItem> {
}
