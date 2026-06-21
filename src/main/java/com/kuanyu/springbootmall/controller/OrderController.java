package com.kuanyu.springbootmall.controller;

import com.kuanyu.springbootmall.dto.CreateOrderRequest;
import com.kuanyu.springbootmall.dto.OrderQueryParms;
import com.kuanyu.springbootmall.model.Order;
import com.kuanyu.springbootmall.service.OrderService;
import com.kuanyu.springbootmall.util.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
public class OrderController {

    @Autowired
    private OrderService orderService ;

    @GetMapping("/users/{userId}/orders")
    public ResponseEntity<Page<Order>> getOrders(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "10") @Min(0) @Max(100) Integer limit,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset
    ) {
        OrderQueryParms orderQueryParms = new OrderQueryParms();
        orderQueryParms.setUserId(userId);
        orderQueryParms.setLimit(limit);
        orderQueryParms.setOffset(offset);

        //取得 order list
        List<Order> orderList = orderService.getOrders(orderQueryParms);

        //取得order 總數
        Integer count = orderService.countOrders(orderQueryParms);

        //分頁
        Page<Order> page = new Page<>();
        page.setLimit(limit);
        page.setOffset(offset);
        page.setTotal(count);
        page.setResults(orderList);

        return ResponseEntity.status(HttpStatus.OK).body(page);
    }

    @PostMapping("/users/{userId}/orders")
    public ResponseEntity<?> createOrder(@PathVariable Integer userId ,
                                             @RequestBody @Valid CreateOrderRequest createOrderRequest) {

       Integer orderId = orderService.createOrder(userId, createOrderRequest);

       Order order = orderService.getOrderById(orderId);

       return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}
