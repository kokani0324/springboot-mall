package com.kuanyu.springbootmall.dao;

import com.kuanyu.springbootmall.dto.OrderQueryParms;
import com.kuanyu.springbootmall.model.Order;
import com.kuanyu.springbootmall.model.OrderItem;

import java.util.List;

public interface OrderDao {

    Integer countOrders(OrderQueryParms orderQueryParms);

    List<Order> getOrders(OrderQueryParms orderQueryParms);

    Integer createOrder(Integer userId, Integer totalAmount);

    void createOrderItem(Integer orderId, List<OrderItem> orderItemList);

    Order getOrderById(Integer orderId);

    List<OrderItem> getOrderItemByOrderId(Integer orderId);
}
