package com.kuanyu.springbootmall.controller;

import com.kuanyu.springbootmall.contstant.ProductCategory;
import com.kuanyu.springbootmall.dto.ProductQueryParms;
import com.kuanyu.springbootmall.dto.ProductRequest;
import com.kuanyu.springbootmall.model.Product;
import com.kuanyu.springbootmall.service.ProductService;

import com.kuanyu.springbootmall.util.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController  //這個 class 裡的方法回傳的物件，
// 不是拿去找 JSP 頁面，而是直接轉成 HTTP response body，通常是 JSON。

public class ProductController {

    @Autowired   //依賴注入 交給spring幫忙實作
    private ProductService productService ;

    //查詢商品類別實作
    @GetMapping("/products")
    public ResponseEntity<Page<Product>> getProducts(
            //required = false category是一個可選的參數
            //@RequestParam 取得URL的參數
            //查詢條件 Filtering
           @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) String search,
            //排序 Sorting
            @RequestParam(defaultValue = "created_date") String orderBy,
            @RequestParam(defaultValue = "desc") String sort,
            //分頁 Pagination
            @RequestParam(defaultValue = "5") @Max(1000) @Min(0) Integer limit,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset
    ) {

        ProductQueryParms productQueryParms = new ProductQueryParms();
        productQueryParms.setCategory(category);
        productQueryParms.setSearch(search);
        productQueryParms.setOrderBy(orderBy);
        productQueryParms.setSort(sort);
        productQueryParms.setLimit(limit);
        productQueryParms.setOffset(offset);

        //取得product list
       List<Product> productList = productService.getProducts(productQueryParms);
        //取得 product count
       Integer total = productService.countProducts(productQueryParms);
        //分頁
       Page<Product> page = new Page<>();

       page.setLimit(limit);
       page.setOffset(offset);
       page.setTotal(total);
       page.setResults(productList);


       return ResponseEntity.status(HttpStatus.OK).body(page);

    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable Integer productId) {
        Product product = productService.getProductById(productId);

        if(product != null) {
            return ResponseEntity.status(HttpStatus.OK).body(product);
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build() ;
        }
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody @Valid ProductRequest productRequest) {

        Integer productId =productService.createProduct(productRequest);

        Product product = productService.getProductById(productId);

        return ResponseEntity.status(HttpStatus.CREATED).body(product);


    }
    @PutMapping("/products/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer productId, @RequestBody @Valid ProductRequest productRequest) {
        //檢查Prodcut是否存在
        Product product = productService.getProductById(productId);

        if(product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();//將responseEntity 傳給前端
        }
        //修改商品的數據
        productService.updateProduct(productId, productRequest);

        Product updatedProduct = productService.getProductById(productId) ;

        return ResponseEntity.status(HttpStatus.OK).body(updatedProduct);

    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer productId) {
        productService.deleteProductById(productId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); //告訴前端刪除的東西已經不存在
        //前端只是要一個結果
    }
}
