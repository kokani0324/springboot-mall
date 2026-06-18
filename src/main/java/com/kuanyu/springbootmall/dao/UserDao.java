package com.kuanyu.springbootmall.dao;

import com.kuanyu.springbootmall.dto.UserRegisterRequest;
import com.kuanyu.springbootmall.model.User;

public interface UserDao {

    User getUserById(Integer userId);

    Integer createUsers(UserRegisterRequest userRegisterRequest) ;
}
