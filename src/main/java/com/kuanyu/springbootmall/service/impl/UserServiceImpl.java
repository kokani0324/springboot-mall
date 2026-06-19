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
import org.springframework.util.DigestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

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

        //使用MD5 生成密碼的雜湊值
        String hashadPassword = DigestUtils.md5DigestAsHex(userRegisterRequest.getPassword().getBytes());
        userRegisterRequest.setPassword(hashadPassword);
        //創建帳號
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
        //使用MD5 生成密碼的雜湊值
        String hashadPassword = DigestUtils.md5DigestAsHex(userLoginRequest.getPassword().getBytes());



        //user.getPassword()資料庫的密碼
        //userLoginRequest.getPassword() 前端使用者的密碼判斷正不正確
        //一定要用equals來比較
        if(user.getPassword().equals(hashadPassword)) {
            return user;
        }else {
            log.warn("email {} 的密碼不正確", userLoginRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
