package com.kuanyu.springbootmall.rowmapper;

import com.kuanyu.springbootmall.contstant.ProductCategory;
import com.kuanyu.springbootmall.model.Product;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductRowMapper  implements RowMapper<Product> {

    @Override
    public Product mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Product product = new Product();

        product.setProductId(resultSet.getInt("product_id"));
        product.setProductName(resultSet.getString("product_name"));

        String categoryStr = resultSet.getString("category");
        ProductCategory category = ProductCategory.valueOf(categoryStr);
        //ProductCategory 是一個 Java 的 Enum（列舉）
        //valueOf() 的機制： 這是 Java 列舉內建的方法。它會拿前一步讀取到的字串 (categoryStr)，去精準比對 ProductCategory 裡面定義的常數名稱。
        product.setCategory(category); //完成資料賦值。您的 Java 物件 (product) 在設計上，其 category 屬性被宣告為 ProductCategory 型別

//        product.setCategory(ProductCategory.valueOf(resultSet.getString("category")));
//          比較簡單的寫法


        product.setImageUrl(resultSet.getString("image_url"));
        product.setPrice(resultSet.getInt("price"));
        product.setStock(resultSet.getInt("stock"));
        product.setDescription(resultSet.getString("description"));
        product.setCreatedDate(resultSet.getTimestamp("created_date"));
        product.setLastModifiedDate(resultSet.getTimestamp("last_modified_date"));



        return product;
    }
}
