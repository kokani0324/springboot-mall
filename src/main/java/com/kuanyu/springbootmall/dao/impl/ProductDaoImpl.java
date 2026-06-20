package com.kuanyu.springbootmall.dao.impl;

import com.kuanyu.springbootmall.dao.ProductDao;
import com.kuanyu.springbootmall.dto.ProductQueryParms;
import com.kuanyu.springbootmall.dto.ProductRequest;
import com.kuanyu.springbootmall.model.Product;
import com.kuanyu.springbootmall.rowmapper.ProductRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProductDaoImpl implements ProductDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate ;

    @Override
    public Integer countProducts(ProductQueryParms productQueryParms) {
        String sql = "select count(*) from product where 1=1";

        Map<String, Object> map = new HashMap<>();
        //WHERE 1=1 AND category = :category，如果是null 就會維持1=1
        //bj
        //查詢條件
        sql = addFilteringSql(sql, map, productQueryParms);

        //取得傳出來的count的值
        //queryForObject的方法可以將count的值轉成Integer total
        Integer total = namedParameterJdbcTemplate.queryForObject(sql, map, Integer.class);

        return total;

    }

    @Override
    public List<Product> getProducts(ProductQueryParms productQueryParms) {
        String sql = "select product_id,product_name, category, image_url, price, stock, description, " +
                "created_date, last_modified_date " +
                "FROM product where 1=1";

        Map<String, Object> map = new HashMap<>();
        //WHERE 1=1 AND category = :category，如果是null 就會維持1=1
        //bj
        //查詢條件
        sql = addFilteringSql(sql, map, productQueryParms);

        //依某某欄位做排序
        sql = sql + " ORDER BY " + productQueryParms.getOrderBy() + " " + productQueryParms.getSort();
        //分頁
        sql = sql + " LIMIT :limit OFFSET :offset";
        map.put("limit", productQueryParms.getLimit());
        map.put("offset", productQueryParms.getOffset());


        //用 namedParameterJdbcTemplate 執行 SQL，查資料庫，然後把結果轉成 List<Product>
        List<Product> productList = namedParameterJdbcTemplate.query(sql,map,new ProductRowMapper());
        //query(sql, map, new ProductRowMapper())
        //SQL 要執行的 SQL 語法//map SQL 裡面的參數//new ProductRowMapper()  把資料庫每一列轉成 Product 物件
        return productList;
    }

    @Override
    public Product getProductById(Integer productId) {
        String sql = "select product_id,product_name, category, image_url, price, stock, description, " +
                "created_date, last_modified_date " +
                "FROM product WHERE product_id = :productId";


        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);

        List<Product> productList = namedParameterJdbcTemplate.query(sql,map,new ProductRowMapper());

        if(productList.size() > 0) {
            return productList.get(0) ;
        }else {
            return null;
        }
    }




    @Override
    public Integer createProduct (ProductRequest productRequest) {
        String sql = "INSERT INTO product(product_name, category, image_url, price, stock," +
                "description , created_date, last_modified_date) VALUES(:productName, :category, :imageUrl, " +
                ":price, :stock, :description , :createdDate, :lastModifiedDate)";

        Map<String, Object> map = new HashMap<>();
        map.put("productName" , productRequest.getProductName());
        map.put("category", productRequest.getCategory().toString());
        map.put("imageUrl", productRequest.getImageUrl());
        map.put("price", productRequest.getPrice());
        map.put("stock", productRequest.getStock());
        map.put("description", productRequest.getDescription());

        Date now = new Date();
        map.put("createdDate", now);
        map.put("lastModifiedDate", now);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder) ;

        int productId = keyHolder.getKey().intValue();

        return productId;
    }

    @Override
    public void updateProduct(Integer productId, ProductRequest productRequest) {
        String sql = "UPDATE product SET product_name = :productName, category = :category, image_url = :imageUrl, " +
                "price = :price, stock = :stock, description = :description, last_modified_date = :lastModifiedDate " +
                "WHERE product_id = :productId";
        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);

        map.put("productName", productRequest.getProductName());
        map.put("category", productRequest.getCategory().toString());
        map.put("imageUrl", productRequest.getImageUrl());
        map.put("price", productRequest.getPrice());
        map.put("stock", productRequest.getStock());
        map.put("description", productRequest.getDescription());

        map.put("lastModifiedDate", new Date()); //new 新的時間點

        namedParameterJdbcTemplate.update(sql, map); // 實際修改方法
    }

    @Override
    public void updateStock(Integer productId, Integer stock) {
        String sql = "UPDATE product SET stock = :stock, last_modified_date = :lastModifiedDate " +
                "WHERE product_id = :productId";

        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);
        map.put("stock", stock);
        map.put("lastModifiedDate", new Date());

        namedParameterJdbcTemplate.update(sql, map);

    }

    @Override
    public void deleteProductById(Integer productId) {
        String sql = "DELETE FROM product WHERE product_id = :productId";
        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);

        namedParameterJdbcTemplate.update(sql, map);
    }

    private String addFilteringSql(String sql, Map<String, Object> map ,ProductQueryParms productQueryParms) {
        //查詢條件
        if(productQueryParms.getCategory() != null) {
            sql = sql + " AND category = :category"; //AND 前面要記得預留空白鍵
            map.put("category", productQueryParms.getCategory().name());
        }

        //LIKE 是模糊查詢的意思
        if(productQueryParms.getSearch() != null) {
            sql = sql + " AND product_name LIKE :search";
            map.put("search", "%" + productQueryParms.getSearch() + "%");
        }
        return sql;
    }
}
