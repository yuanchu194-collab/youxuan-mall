package com.youxuan.order.service;

import com.youxuan.common.result.PageResult;
import com.youxuan.order.dto.OrderCreateRequest;
import com.youxuan.order.vo.OrderDetailVO;
import com.youxuan.order.vo.OrderPageVO;

/**
 * 订单服务。
 */
public interface OrderService {

    OrderDetailVO create(OrderCreateRequest request);

    OrderDetailVO detail(Long id);

    PageResult<OrderPageVO> myOrders(Long pageNum, Long pageSize);
}
