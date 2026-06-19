package com.kuanyu.springbootmall.service.impl;

import com.kuanyu.springbootmall.dao.UserDao;
import com.kuanyu.springbootmall.dto.UserLoginRequest;
import com.kuanyu.springbootmall.dto.UserRegisterRequest;
import com.kuanyu.springbootmall.model.User;
import com.kuanyu.springbootmall.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UserServiceImpl implements UserService {
    //檢查註冊的email
    private final static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserDao userDao ;

    @Override
    public User getUserById(Integer userId) {
        return userDao.getUserById(userId);
    }

    @Override
    public Integer register(UserRegisterRequest userRegisterRequest) {
        User user = userDao.getUserByEmail(userRegisterRequest.getEmail());

        //判斷mail有沒有註冊過
        if(user != null) {
            log.warn("該email {} 已經被註冊", userRegisterRequest.getEmail() );
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return userDao.createUsers(userRegisterRequest);
    }

    @Override
    public User login(UserLoginRequest userLoginRequest) {

        User user = userDao.getUserByEmail(userLoginRequest.getEmail());
        //先判斷email
        if(user == null) {
            log.warn("該email {}尚未註冊 ", userLoginRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        //user.getPassword()資料庫的密碼
        //userLoginRequest.getPassword() 前端使用者的密碼判斷正不正確
        //一定要用equals來比較
        if(user.getPassword().equals(userLoginRequest.getPassword())) {
            return user;
        }else {
            log.warn("email {} 的密碼不正確", userLoginRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
