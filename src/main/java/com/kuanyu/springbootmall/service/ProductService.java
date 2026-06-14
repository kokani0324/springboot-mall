package com.kuanyu.springbootmall.service;

import com.kuanyu.springbootmall.dto.ProductRequest;
import com.kuanyu.springbootmall.model.Product;

public interface ProductService {

    Product getProductById(Integer productId) ;

    Integer createProduct(ProductRequest productRequest) ;

    void updateProduct(Integer productId, ProductRequest productRequest) ;
}
