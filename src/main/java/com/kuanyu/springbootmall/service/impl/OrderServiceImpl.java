package com.kuanyu.springbootmall.service.impl;

import com.kuanyu.springbootmall.dao.OrderDao;
import com.kuanyu.springbootmall.dao.ProductDao;
import com.kuanyu.springbootmall.dto.BuyItem;
import com.kuanyu.springbootmall.dto.CreateOrderRequest;
import com.kuanyu.springbootmall.model.Order;
import com.kuanyu.springbootmall.model.OrderItem;
import com.kuanyu.springbootmall.model.Product;
import com.kuanyu.springbootmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private ProductDao productDao;

    @Transactional
    @Override
    public Integer createOrder(Integer userId, CreateOrderRequest createOrderRequest) {
        int totalAmount = 0;
        List<OrderItem> orderItemList = new ArrayList<>() ;

        for (BuyItem buyItem : createOrderRequest.getBuyItemsList()) {
            Product product = productDao.getProductById(buyItem.getProductId());

            //計算總價錢
            int amount = buyItem.getQuantity() * product.getPrice();
            totalAmount += amount;

            //轉換BuyItem to OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(buyItem.getProductId());
            orderItem.setQuantity(buyItem.getQuantity());
            orderItem.setAmount(amount);

            orderItemList.add(orderItem);

        }

        //創建訂單
        Integer orderId = orderDao.createOrder(userId, totalAmount);

         orderDao.createOrderItem(orderId, orderItemList);

        return orderId;

    }

    @Override
    public Order getOrderById(Integer orderId) {
        Order order = orderDao.getOrderById(orderId);

        List<OrderItem> orderItemList = orderDao.getOrderItemByOrderId(orderId);

        order.setOrderItemList(orderItemList);

        return order;
    }
}
