package com.kuanyu.springbootmall.service;

import com.kuanyu.springbootmall.dto.ProductQueryParms;
import com.kuanyu.springbootmall.dto.ProductRequest;
import com.kuanyu.springbootmall.model.Product;

import java.util.List;

public interface ProductService {

    Integer countProducts(ProductQueryParms productQueryParms) ;

    List<Product> getProducts(ProductQueryParms productQueryParms) ;

    Product getProductById(Integer productId) ;

    Integer createProduct(ProductRequest productRequest) ;

    void updateProduct(Integer productId, ProductRequest productRequest) ;

    void deleteProductById(Integer productId) ;
}
