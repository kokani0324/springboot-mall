package com.kuanyu.springbootmall.dto;

import com.kuanyu.springbootmall.contstant.ProductCategory;

public class ProductQueryParms {

    private ProductCategory category ;
    private String search ;

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
