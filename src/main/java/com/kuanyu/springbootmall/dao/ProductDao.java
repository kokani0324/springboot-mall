package com.kuanyu.springbootmall.dao;

import com.kuanyu.springbootmall.dto.ProductRequest;
import com.kuanyu.springbootmall.model.Product;

import java.util.List;

public interface ProductDao {
//Dao 是 Data Access Object，專門負責資料庫存取。

    List<Product> getProducts() ;

    Product getProductById(Integer productId) ;

    Integer createProduct(ProductRequest productRequest) ;

    void updateProduct(Integer productId, ProductRequest productRequest) ;

    void deleteProductById(Integer productId) ;

}
