package com.miaoshaproject.service;

import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.service.model.UserModel;

public interface UserService {
    UserModel getUserById(Integer id);
    void userRegister(UserModel userModel)throws BussinessException;
    UserModel validateUser(String telphone, String password)throws BussinessException;
}
