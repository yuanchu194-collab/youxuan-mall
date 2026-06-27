package com.youxuan.order.service;

import com.youxuan.common.result.PageResult;
import com.youxuan.common.message.OrderTimeoutMessage;
import com.youxuan.order.dto.OrderCreateRequest;
import com.youxuan.order.dto.OrderShipRequest;
import com.youxuan.order.vo.OrderDetailVO;
import com.youxuan.order.vo.OrderPageVO;

/**
 * 订单服务。
 */
public interface OrderService {

    OrderDetailVO create(OrderCreateRequest request);

    OrderDetailVO detail(Long id);

    PageResult<OrderPageVO> myOrders(Long pageNum, Long pageSize);

    OrderDetailVO pay(Long id);

    OrderDetailVO cancel(Long id);

    OrderDetailVO ship(Long id, OrderShipRequest request);

    OrderDetailVO receive(Long id);

    void timeoutCancel(OrderTimeoutMessage message);
}
