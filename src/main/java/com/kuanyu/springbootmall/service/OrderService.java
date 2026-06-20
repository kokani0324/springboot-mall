package com.kuanyu.springbootmall.service;


import com.kuanyu.springbootmall.dto.CreateOrderRequest;
import com.kuanyu.springbootmall.model.Order;

public interface OrderService {

    Integer createOrder(Integer userId, CreateOrderRequest createOrderRequest);

    Order getOrderById(Integer orderId);

}
