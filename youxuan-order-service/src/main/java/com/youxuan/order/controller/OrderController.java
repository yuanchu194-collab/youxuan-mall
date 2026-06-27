package com.youxuan.order.controller;

import com.youxuan.common.result.PageResult;
import com.youxuan.common.result.Result;
import com.youxuan.order.dto.OrderCreateRequest;
import com.youxuan.order.service.OrderService;
import com.youxuan.order.vo.OrderDetailVO;
import com.youxuan.order.vo.OrderPageVO;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单接口。当前阶段只提供创建待支付订单和查询能力。
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<OrderDetailVO> create(@Valid @RequestBody OrderCreateRequest request) {
        return Result.success(orderService.create(request));
    }

    @GetMapping("/{id:\\d+}")
    public Result<OrderDetailVO> detail(@PathVariable("id") Long id) {
        return Result.success(orderService.detail(id));
    }

    @GetMapping("/my")
    public Result<PageResult<OrderPageVO>> myOrders(
            @RequestParam(name = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.success(orderService.myOrders(pageNum, pageSize));
    }
}
