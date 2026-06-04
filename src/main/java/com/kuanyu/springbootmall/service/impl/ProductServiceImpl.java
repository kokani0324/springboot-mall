package com.kuanyu.springbootmall.service.impl;

import com.kuanyu.springbootmall.dao.ProductDao;
import com.kuanyu.springbootmall.model.Product;
import com.kuanyu.springbootmall.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductServiceImpl  implements ProductService {

    @Autowired
    private ProductDao productDao ;
    @Override
    public Product getProductById(Integer productId) {
        return productDao.getProductById(productId);
    }
}
