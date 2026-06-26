package com.youxuan.order.service;

import com.youxuan.order.dto.OrderConfirmRequest;
import com.youxuan.order.vo.OrderConfirmVO;

/**
 * 订单确认页服务。
 */
public interface OrderConfirmService {

    OrderConfirmVO confirm(OrderConfirmRequest request);
}
