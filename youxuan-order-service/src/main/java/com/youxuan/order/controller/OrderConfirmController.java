package com.youxuan.order.controller;

import com.youxuan.common.result.Result;
import com.youxuan.order.dto.OrderConfirmRequest;
import com.youxuan.order.service.OrderConfirmService;
import com.youxuan.order.vo.OrderConfirmVO;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单确认页接口。
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderConfirmController {

    private final OrderConfirmService orderConfirmService;

    public OrderConfirmController(OrderConfirmService orderConfirmService) {
        this.orderConfirmService = orderConfirmService;
    }

    @PostMapping(value = "/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<OrderConfirmVO> confirm(@Valid @RequestBody OrderConfirmRequest request) {
        return Result.success(orderConfirmService.confirm(request));
    }
}
