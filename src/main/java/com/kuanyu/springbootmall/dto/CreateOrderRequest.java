package com.kuanyu.springbootmall.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class CreateOrderRequest {
    //這個註解是用來驗證這個集合不能是空的
    @NotEmpty
    private List<BuyItem> buyItemsList ;

    public List<BuyItem> getBuyItemsList() {
        return buyItemsList;
    }

    public void setBuyItemsList(List<BuyItem> buyItemsList) {
        this.buyItemsList = buyItemsList;
    }
}
