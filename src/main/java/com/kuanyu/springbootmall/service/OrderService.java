package com.kuanyu.springbootmall.service;


import com.kuanyu.springbootmall.dto.CreateOrderRequest;
import com.kuanyu.springbootmall.dto.OrderQueryParms;
import com.kuanyu.springbootmall.model.Order;

import java.util.List;

public interface OrderService {

    Integer countOrders(OrderQueryParms orderQueryParms);

    List<Order> getOrders(OrderQueryParms orderQueryParms);

    Integer createOrder(Integer userId, CreateOrderRequest createOrderRequest);

    Order getOrderById(Integer orderId);

}
