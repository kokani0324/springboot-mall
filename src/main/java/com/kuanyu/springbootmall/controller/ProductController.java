package com.kuanyu.springbootmall.controller;

import com.kuanyu.springbootmall.model.Product;
import com.kuanyu.springbootmall.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController  //這個 class 裡的方法回傳的物件，
// 不是拿去找 JSP 頁面，而是直接轉成 HTTP response body，通常是 JSON。

public class ProductController {

    @Autowired   //依賴注入 交給spring幫忙實作
    private ProductService productService ;

    @GetMapping("/products/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable Integer productId) {
        Product product = productService.getProductById(productId);

        if(product != null) {
            return ResponseEntity.status(HttpStatus.OK).body(product);
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build() ;
        }
    }
}
