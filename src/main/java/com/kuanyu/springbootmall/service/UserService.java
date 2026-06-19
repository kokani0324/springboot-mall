package com.kuanyu.springbootmall.service;

import com.kuanyu.springbootmall.dto.UserLoginRequest;
import com.kuanyu.springbootmall.dto.UserRegisterRequest;
import com.kuanyu.springbootmall.model.User;

public interface UserService {

    User getUserById(Integer userId);

    Integer register (UserRegisterRequest userRegisterRequest);

    User login (UserLoginRequest userLoginRequest);
}
