package com.kuanyu.springbootmall.service.impl;

import com.kuanyu.springbootmall.dao.UserDao;
import com.kuanyu.springbootmall.dto.UserRegisterRequest;
import com.kuanyu.springbootmall.model.User;
import com.kuanyu.springbootmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao ;

    @Override
    public User getUserById(Integer userId) {
        return userDao.getUserById(userId);
    }

    @Override
    public Integer register(UserRegisterRequest userRegisterRequest) {
        return userDao.createUsers(userRegisterRequest);
    }
}
